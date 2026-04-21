package yin.xuebiblockchain.Service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import yin.xuebiblockchain.Mapper.FileUploadMapper;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.FileUploadService;
import yin.xuebiblockchain.Utils.UserHolder;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.file-host}")
    private String fileHost;

    @Resource
    private FileUploadMapper fileUploadMapper;

    @Override
    public Result saveIcon(MultipartFile file) {
        // 1. 创建 OSS 客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String url = "";
        try {
            // 2. 获取文件上传流
            InputStream inputStream = file.getInputStream();

            // 3. 生成文件名，避免重名
            String fileName = fileHost + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 4. 上传文件到指定 Bucket
            ossClient.putObject(bucketName, fileName, inputStream);

            // 5. 拼接文件 URL
            url = "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6. 关闭 OSS 客户端
            ossClient.shutdown();
        }

        //获取用户名
        Long userId = UserHolder.getUser().getId();

        //存icon
        fileUploadMapper.save(userId,url);


        return Result.success(url);  // 返回文件的 URL
    }
}
