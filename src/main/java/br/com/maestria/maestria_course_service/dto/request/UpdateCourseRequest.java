package br.com.maestria.maestria_course_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCourseRequest {
    private String title;
    private String description;
    private BigDecimal price;
}