package com.example.maum.service;

import org.springframework.web.multipart.MultipartFile;

// ★ 즐겨찾기 이후 추가/수정
public interface ISttService {

    String transcribe(MultipartFile audio) throws Exception;
}
