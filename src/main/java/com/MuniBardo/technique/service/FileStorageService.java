package com.MuniBardo.technique.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service // <-- تأكد من وجود هذا التعليق هنا لتسجيله كـ Bean
public class FileStorageService {


private final Path fileStorageLocation = Paths.get("uploads/documents").toAbsolutePath().normalize();

public FileStorageService() {
    try {
        Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
        throw new RuntimeException("تعذر إنشاء المجلد الخاص بتخزين الملفات والمرفقات.", ex);
    }
}

public String storeFile(MultipartFile file) {
    try {
        if (file.isEmpty()) {
            throw new RuntimeException("الملف المرفق فارغ.");
        }

        // توليد اسم فريد للملف لمنع تضارب الأسماء
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = this.fileStorageLocation.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    } catch (IOException ex) {
        throw new RuntimeException("فشل حفظ الملف على السيرفر.", ex);
    }
}
}
