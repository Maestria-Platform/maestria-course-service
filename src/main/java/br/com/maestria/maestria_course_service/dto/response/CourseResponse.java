package br.com.maestria.maestria_course_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private UUID instructorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}