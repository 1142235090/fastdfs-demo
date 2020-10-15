package com.example.demo.controller;

import com.example.demo.bean.FastDFSFile;
import com.example.demo.util.FastDFSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class UploadController {
    private static Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${fastdfs_nginx_url:''}")
    private String fastdfsNginxUrl;

    @GetMapping("/test")
    public String index() {
        return "upload";
    }

    @Autowired
    private FastDFSService fastDFSService;

    @PostMapping("/delete")
    public Integer delete(String groupName, String remoteFileName) {
        try {
            return fastDFSService.deleteFile(groupName,remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param multipartFile
     * @return
     * @throws IOException 这个方法后面可以自己封装一个util,这里只是测试
     */
    @PostMapping("/save")
    public String saveFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String[] fileAbsolutePath={};
        String fileName=multipartFile.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        byte[] file_buff = null;
        InputStream inputStream=multipartFile.getInputStream();
        if(inputStream!=null){
            int len1 = inputStream.available();
            file_buff = new byte[len1];
            inputStream.read(file_buff);
        }
        inputStream.close();
        FastDFSFile file = new FastDFSFile(fileName, file_buff, ext);
        try {
            fileAbsolutePath = fastDFSService.upload(file);  //upload to fastdfs
        } catch (Exception e) {
            logger.error("upload file Exception!",e);
        }
        if (fileAbsolutePath==null) {
            logger.error("upload file failed,please upload again!");
        }
        String path= fastdfsNginxUrl+"/"+fileAbsolutePath[0]+ "/"+fileAbsolutePath[1];
        return path;
    }
}
