package com.liangheng.dto;

import lombok.Data;

@Data
public class BlogEvent {
    private Long blogId;
    private Long userId;
    private Long timestamp;
}


