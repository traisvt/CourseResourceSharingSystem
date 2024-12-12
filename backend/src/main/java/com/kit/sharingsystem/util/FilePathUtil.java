package com.kit.sharingsystem.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FilePathUtil {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public Path getUploadPath() {
        // 获取当前项目根目录
        Path projectRoot = Paths.get("").toAbsolutePath();
        return projectRoot.resolve(uploadDir);
    }

    public Path getResourcePath(String fileName) {
        return getUploadPath().resolve(fileName);
    }

    public String getRelativePath(String fileName) {
        return uploadDir + "/" + fileName;
    }
}
