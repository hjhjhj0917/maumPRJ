package com.example.maum.service;

import org.springframework.web.multipart.MultipartFile;

public interface IGcsService {

    /*
    이미지를 GCS에 업로드하고 공개 접근 URL을 반환함
    */
    String uploadImage(MultipartFile file, Integer diaryNo) throws Exception;

    /*
    GCS에 업로드된 이미지를 삭제함
    */
    void deleteImage(String imageUrl);
}
