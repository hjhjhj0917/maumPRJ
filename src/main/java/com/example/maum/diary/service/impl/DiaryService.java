package com.example.maum.diary.service.impl;

import com.example.maum.diary.dto.DiaryDTO;
import com.example.maum.diary.repository.DiaryRepository;
import com.example.maum.diary.repository.entity.DiaryEntity;
import com.example.maum.diary.service.IDiaryService;
import com.example.maum.global.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private final DiaryRepository diaryRepository;

    /*
    일기 저장
    */
    @Override
    public int diaryInsert(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        int res = 0;

        try {
            String createdAt = CmmUtil.nvl(pDTO.createdAt());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
            LocalDate parsedDate = LocalDate.parse(createdAt, formatter);

            DiaryEntity pEntity = DiaryEntity.builder()
                    .userNo(pDTO.userNo())
                    .title(CmmUtil.nvl(pDTO.title()))
                    .content(CmmUtil.nvl(pDTO.content()))
                    .createdAt(parsedDate)
                    .build();

            diaryRepository.save(pEntity);

            res = 1;
            log.info("일기 저장 성공!");

        } catch (Exception e) {
            res = 2;
            log.error("일기 저장 중 오류 발생 : {}", e.getMessage());
        }

        return res;
    }
}
