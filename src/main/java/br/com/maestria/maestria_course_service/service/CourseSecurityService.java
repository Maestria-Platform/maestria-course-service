package br.com.maestria.maestria_course_service.service;


import br.com.maestria.maestria_course_service.entity.Course;
import br.com.maestria.maestria_course_service.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("courseSecurity")
@RequiredArgsConstructor
public class CourseSecurityService {

    private final CourseRepository courseRepository;

    public boolean isInstructorOfCourse(Jwt jwt, UUID courseId) {
        UUID instructorId = UUID.fromString(jwt.getSubject());
        return courseRepository.findById(courseId)
                .map(Course::getInstructorId)
                .map(ownerId -> ownerId.equals(instructorId))
                .orElse(false);
    }
}