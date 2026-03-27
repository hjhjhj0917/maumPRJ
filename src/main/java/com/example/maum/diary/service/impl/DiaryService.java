package com.example.maum.diary.service.impl;

import com.example.maum.diary.repository.DiaryRepository;
import com.example.maum.diary.service.IDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private final DiaryRepository diaryRepository;
}
