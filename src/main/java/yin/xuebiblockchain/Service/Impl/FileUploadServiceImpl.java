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

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Resource
    private FileUploadMapper fileUploadMapper;

    @Override
    public Result saveIcon(MultipartFile file) {
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        if (userId == null) return Result.error("请先登录");

        try {
            // 1. 创建本地存储目录
            String realDir = System.getProperty("user.dir") + "/" + uploadDir;
            File dir = new File(realDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 3. 构造访问URL（通过静态资源映射访问）
            String url = "/avatars/" + fileName;

            fileUploadMapper.save(userId, url);
            return Result.success(url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
