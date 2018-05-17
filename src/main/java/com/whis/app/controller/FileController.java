package com.whis.app.controller;

import com.whis.base.common.DataResponse;
import com.whis.base.exception.BaseException;
import com.whis.base.exception.WrongParamException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

@RestController("FileController")
@RequestMapping("/file")
@Component
public class FileController {

    private static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload.root}")
    private String fileRoot;

    @Value("${file.upload.image.mime.type}")
    private String allowedImageMimeType;

    @Value("${file.upload.image.size.max}")
    private Long maxImageSize;

    private Path getImageFilePath(String fileName)
    {
        return Paths.get(fileRoot, "img", fileName);
    }

    @RequestMapping("/img/upload")
    public DataResponse handleFileUpload(@RequestParam("file") MultipartFile file)
    {
        DataResponse response = DataResponse.create();
        if (file.isEmpty())
        {
            throw new WrongParamException("文件为空");
        }

        if (!allowedImageMimeType.contains(file.getContentType()))
        {
            throw new WrongParamException("文件类型不符");
        }

        logger.info("max Image Size: {}", maxImageSize);
        if (file.getSize() > maxImageSize)
        {
            throw new WrongParamException("文件大小超过最大限制");
        }

        try
        {
            File directory = new File(String.format("%s/img", fileRoot));
            Files.createDirectories(directory.toPath());
            String fileName = DigestUtils.md5Hex(file.getInputStream()) + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Files.copy(file.getInputStream(), getImageFilePath(fileName), StandardCopyOption.REPLACE_EXISTING);

            HashMap<String, Object> fileDataMap = new HashMap<>();
            fileDataMap.put("name", fileName);
            response.put("file", fileDataMap);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            throw new BaseException(-1, "文件上传失败 ");
        }

        return response;
    }

}
