package br.com.maestria.maestria_course_service.service;


import br.com.maestria.maestria_course_service.dto.request.CreateCourseRequest;
import br.com.maestria.maestria_course_service.dto.request.UpdateCourseRequest;
import br.com.maestria.maestria_course_service.entity.Course;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseService {
    Course createCourse(CreateCourseRequest createCourseRequest, Jwt jwt);
    Optional<Course> findById(UUID courseId);
    List<Course> findAll();
    Course updateCourse(UUID courseId, UpdateCourseRequest updateCourseRequest, Jwt jwt);
    void deleteCourse(UUID courseId, Jwt jwt);
}