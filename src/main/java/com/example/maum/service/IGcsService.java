package com.example.maum.service;

import org.springframework.web.multipart.MultipartFile;

public interface IGcsService {

    String uploadImage(MultipartFile file, Integer diaryNo) throws Exception;

    void deleteImage(String imageUrl);
}
