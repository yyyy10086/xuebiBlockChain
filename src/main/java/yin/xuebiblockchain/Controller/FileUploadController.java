package yin.xuebiblockchain.Controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import yin.xuebiblockchain.Pojo.Result;
import yin.xuebiblockchain.Service.FileUploadService;

import java.util.UUID;

import java.io.InputStream;

@Slf4j
@RestController
public class FileUploadController {
    @Resource
    private FileUploadService fileUploadService;


    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(MultipartFile file){
        return fileUploadService.saveIcon(file);
    }
}
