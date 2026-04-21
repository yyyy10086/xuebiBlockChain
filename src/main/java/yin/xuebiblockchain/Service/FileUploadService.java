package yin.xuebiblockchain.Service;

import org.springframework.web.multipart.MultipartFile;
import yin.xuebiblockchain.Pojo.Result;


public interface FileUploadService {
    Result saveIcon(MultipartFile file);
}
