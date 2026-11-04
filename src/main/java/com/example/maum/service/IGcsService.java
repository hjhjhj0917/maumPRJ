package com.example.maum.service;

import org.springframework.web.multipart.MultipartFile;

public interface IGcsService {

    // 업로드 후 저장 경로(storage_key)를 돌려줌
    String uploadImage(MultipartFile file, String diaryPublicId) throws Exception;

    // 저장 경로를 화면에 보낼 주소로 바꿈
    String toUrl(String storageKey);

    void deleteImage(String storageKey);
}
