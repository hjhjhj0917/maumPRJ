package com.example.example.analysis.controller;

import com.example.example.analysis.service.IClovaServiece;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/api/clova")
@RequiredArgsConstructor
public class ClovaController {

    private final IClovaServiece clovaServiece;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, String> request) {

        log.info("{}.analyze Start!", this.getClass().getName());

        String content = request.get("content");
        Map<String, Object> result = clovaServiece.analyzeWithThinking(content);

        log.info("{}.analyze End!", this.getClass().getName());

        return ResponseEntity.ok(result);
    }
}
