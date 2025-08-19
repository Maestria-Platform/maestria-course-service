package br.com.maestria.maestria_course_service.dto.request;

import lombok.Data;

@Data
public class UpdateCourseRequest {
    private String title;
    private String description;
}