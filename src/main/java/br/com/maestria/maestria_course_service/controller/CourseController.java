package br.com.maestria.maestria_course_service.controller;


import br.com.maestria.maestria_course_service.dto.request.CreateCourseRequest;
import br.com.maestria.maestria_course_service.dto.request.UpdateCourseRequest;
import br.com.maestria.maestria_course_service.dto.response.CourseResponse;
import br.com.maestria.maestria_course_service.entity.Course;
import br.com.maestria.maestria_course_service.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/courses")
@Slf4j
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody CreateCourseRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("Controller recebeu requisição para criar curso: {}", request.getTitle());

        Course savedCourse = courseService.createCourse(request, jwt);

        return ResponseEntity.status(201).body(savedCourse);
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        List<CourseResponse> response = courses.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable UUID id) {
        return courseService.findById(id)
                .map(this::convertToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable UUID id, @RequestBody UpdateCourseRequest request, @AuthenticationPrincipal Jwt jwt) {
        Course updatedCourse = courseService.updateCourse(id, request, jwt);
        return ResponseEntity.ok(convertToResponseDto(updatedCourse));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        courseService.deleteCourse(id, jwt);
    }

    private CourseResponse convertToResponseDto(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .instructorId(course.getInstructorId())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}