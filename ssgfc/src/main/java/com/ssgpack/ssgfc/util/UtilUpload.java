package com.ssgpack.ssgfc.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UtilUpload {

    @Value("${resource.path}")
    private String basePath;

    // 일반 업로드
    public String fileUpload(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) return null;

        String saveName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File dir = new File(basePath + subDir);
        if (!dir.exists()) dir.mkdirs();

        File target = new File(dir, saveName);
        FileCopyUtils.copy(file.getBytes(), target);
        return saveName;
    }

    //  유저 프로필 전용 업로드
    public String uploadUserProfile(MultipartFile file) throws IOException {
    	return fileUpload(file, "userimage/");
    }
}
