package yin.xuebiblockchain.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FabricCliService {

    @Value("${fabric.cli.path:/Users/hypoxic/fabric-samples/test-network}")
    private String fabricNetworkPath;

    @Value("${fabric.cli.bin:/Users/hypoxic/fabric-samples/bin}")
    private String fabricBinPath;

    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "campus";
    private static final String ORDERER_ADDRESS = "localhost:7050";
    private static final String ORDERER_HOST = "orderer.example.com";
    private static final String PEER1_ADDRESS = "localhost:7051";
    private static final String PEER2_ADDRESS = "localhost:9051";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tlsCaFile;
    private String peer1TlsFile;
    private String peer2TlsFile;
    private String peerExecutable;

    @PostConstruct
    public void init() {
        String orgPath = fabricNetworkPath + "/organizations";
        this.tlsCaFile = orgPath + "/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem";
        this.peer1TlsFile = orgPath + "/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt";
        this.peer2TlsFile = orgPath + "/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt";
        this.peerExecutable = "/Users/hypoxic/fabric-samples/bin/peer";

        File peerFile = new File(peerExecutable);
        if (!peerFile.exists()) {
            log.error("peer 可执行文件不存在: {}", peerExecutable);
            throw new RuntimeException("Fabric peer 命令未找到，请确认 fabric.cli.bin 配置正确");
        }
        log.info("Fabric CLI 服务初始化完成，peer路径: {}, 网络路径: {}", peerExecutable, fabricNetworkPath);
    }

    private String executeInvoke(String function, String... args) {
        List<String> command = buildInvokeCommand();
        command.add("-c");
        command.add(buildChaincodeArgs(function, args));
        return executeCommand(command);
    }

    private String executeQuery(String function, String... args) {
        List<String> command = buildQueryCommand();
        command.add("-c");
        command.add(buildChaincodeArgs(function, args));
        return executeCommand(command);
    }

    private List<String> buildInvokeCommand() {
        List<String> cmd = new ArrayList<>();
        cmd.add(peerExecutable);
        cmd.add("chaincode");
        cmd.add("invoke");
        cmd.add("-o");
        cmd.add(ORDERER_ADDRESS);
        cmd.add("--ordererTLSHostnameOverride");
        cmd.add(ORDERER_HOST);
        cmd.add("--tls");
        cmd.add("--cafile");
        cmd.add(tlsCaFile);
        cmd.add("-C");
        cmd.add(CHANNEL_NAME);
        cmd.add("-n");
        cmd.add(CHAINCODE_NAME);
        cmd.add("--peerAddresses");
        cmd.add(PEER1_ADDRESS);
        cmd.add("--tlsRootCertFiles");
        cmd.add(peer1TlsFile);
        cmd.add("--peerAddresses");
        cmd.add(PEER2_ADDRESS);
        cmd.add("--tlsRootCertFiles");
        cmd.add(peer2TlsFile);
        return cmd;
    }

    private List<String> buildQueryCommand() {
        List<String> cmd = new ArrayList<>();
        cmd.add(peerExecutable);
        cmd.add("chaincode");
        cmd.add("query");
        // query 也需要指定 Orderer 信息
        cmd.add("-o");
        cmd.add(ORDERER_ADDRESS);
        cmd.add("--ordererTLSHostnameOverride");
        cmd.add(ORDERER_HOST);
        cmd.add("--tls");
        cmd.add("--cafile");
        cmd.add(tlsCaFile);
        cmd.add("-C");
        cmd.add(CHANNEL_NAME);
        cmd.add("-n");
        cmd.add(CHAINCODE_NAME);
        // query 只能连接一个 peer
        cmd.add("--peerAddresses");
        cmd.add(PEER1_ADDRESS);
        cmd.add("--tlsRootCertFiles");
        cmd.add(peer1TlsFile);
        return cmd;
    }

    private String buildChaincodeArgs(String function, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"function\":\"").append(function).append("\",\"Args\":[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(args[i])).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String executeCommand(List<String> command) {
        try {
            log.debug("执行命令: {}", String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(fabricNetworkPath));

            pb.environment().put("FABRIC_CFG_PATH", fabricNetworkPath + "/../config");
            pb.environment().put("CORE_PEER_TLS_ENABLED", "true");
            pb.environment().put("CORE_PEER_LOCALMSPID", "Org1MSP");
            pb.environment().put("CORE_PEER_TLS_ROOTCERT_FILE", peer1TlsFile);
            pb.environment().put("CORE_PEER_MSPCONFIGPATH",
                    fabricNetworkPath + "/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp");
            pb.environment().put("CORE_PEER_ADDRESS", PEER1_ADDRESS);

            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("命令执行超时");
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            if (process.exitValue() != 0 || output.contains("Error:") || output.contains("endorsement failure")) {
                String errorMessage = error.isEmpty() ? output : error;
                log.error("链码命令失败，exitCode: {}, 输出: {}", process.exitValue(), errorMessage);
                throw new RuntimeException("区块链命令执行失败: " + errorMessage);
            }



            return extractJsonFromOutput(output);
        } catch (Exception e) {
            log.error("执行 Fabric CLI 命令失败", e);
            throw new RuntimeException("区块链操作失败: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromOutput(String output) {
        int payloadIndex = output.indexOf("payload:\"");
        if (payloadIndex != -1) {
            int start = output.indexOf("\"", payloadIndex) + 1;
            int end = output.indexOf("\"", start);
            if (end > start) {
                String escapedJson = output.substring(start, end);
                return escapedJson.replace("\\\"", "\"").replace("\\\\", "\\");
            }
        }
        String[] lines = output.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.startsWith("{") || line.startsWith("[")) {
                return line;
            }
        }
        if (output.contains("Error:") || output.contains("endorsement failure")) {
            throw new RuntimeException("链码调用失败，原始输出: " + output);
        }
        return output;
    }

    // ==================== 业务方法 ====================
    public String publishResource(String resourceId, String name, String owner, int pointsCost, String metadata) {
        return executeInvoke("PublishResource", resourceId, name, owner, String.valueOf(pointsCost), metadata);
    }

    /**
     * 借用申请（调用链码 RequestBorrow）
     * @param resourceId 资源ID
     * @param borrower 借用者公钥地址
     */
    public String requestBorrow(String resourceId, String borrower) {
        return executeInvoke("RequestBorrow", resourceId, borrower);
    }

    /**
     * 确认收到（调用链码 ConfirmReceived）
     * @param resourceId 资源ID
     */
    public String confirmReceived(String resourceId) throws Exception {
        return invokeAndWait("LENT", "ConfirmReceived", resourceId);
    }



    /**
     * 轮询等待资源状态变为预期值
     * @param resourceId 资源ID
     * @param expectedStatus 期望的状态 (LENT / AVAILABLE)
     * @param timeoutSeconds 超时秒数
     */
    private void waitForStatusChange(String resourceId, String expectedStatus, int timeoutSeconds) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        String currentStatus = null;
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(1500); // 每 1.5 秒查询一次
            String json = readResource(resourceId);
            if (json != null && !json.isEmpty()) {
                // 简单提取 status 字段
                int idx = json.indexOf("\"status\":\"");
                if (idx != -1) {
                    int start = idx + 10;
                    int end = json.indexOf("\"", start);
                    if (end != -1) {
                        currentStatus = json.substring(start, end);
                        if (expectedStatus.equals(currentStatus)) {
                            log.info("资源 {} 状态已更新为 {}", resourceId, expectedStatus);
                            return;
                        }
                    }
                }
            }
            log.debug("等待资源 {} 状态变为 {}，当前: {}", resourceId, expectedStatus, currentStatus);
        }
        throw new RuntimeException(String.format("等待资源 %s 状态变为 %s 超时，当前状态: %s", resourceId, expectedStatus, currentStatus));
    }

    /**
     * 执行 invoke 并等待链上状态变更为预期值
     */
    private String invokeAndWait(String expectedStatus, String function, String... args) throws Exception {
        String result = executeInvoke(function, args);
        // 第一个参数必须是 resourceId（根据链码方法约定）
        if (args.length > 0) {
            waitForStatusChange(args[0], expectedStatus, 25); // 最多等 25 秒
        }
        return result;
    }

    /**
     * 归还资源（调用链码 ConfirmReturn）
     * @param resourceId 资源ID
     */
    public String confirmReturn(String resourceId) throws Exception {
        return invokeAndWait("AVAILABLE", "ConfirmReturn", resourceId);
    }
    public String readResource(String resourceId) {
        return executeQuery("ReadResource", resourceId);
    }

    public String getResourceHistory(String resourceId) {
        return executeQuery("GetResourceHistory", resourceId);
    }
}