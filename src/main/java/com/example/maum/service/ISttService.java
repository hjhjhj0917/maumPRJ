package com.example.maum.service;

import org.springframework.web.multipart.MultipartFile;

public interface ISttService {

    String transcribe(MultipartFile audio) throws Exception;
}
