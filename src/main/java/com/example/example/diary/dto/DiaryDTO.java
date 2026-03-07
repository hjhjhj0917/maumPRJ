package com.example.example.diary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryDTO {

    private String userId;
    private String title;
    private String content;
    private String date;
}
