package br.com.maestria.maestria_course_service.service;

import br.com.maestria.maestria_course_service.dto.request.CreateCourseRequest;
import br.com.maestria.maestria_course_service.dto.request.UpdateCourseRequest;
import br.com.maestria.maestria_course_service.entity.Course;
import br.com.maestria.maestria_course_service.exception.ResourceNotFoundException;
import br.com.maestria.maestria_course_service.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService{

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public Course createCourse(CreateCourseRequest createCourseRequest, Jwt jwt) {
        log.info("Iniciando a lógica de criação do curso: {}", createCourseRequest.getTitle());

        UUID instructorId = UUID.fromString(jwt.getSubject());
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));

        log.info("Curso a ser criado pelo instrutor ID: {} no tenant ID: {}", instructorId, tenantId);

        Course newCourse = Course.builder()
                .title(createCourseRequest.getTitle())
                .description(createCourseRequest.getDescription())
                .instructorId(instructorId)
                .tenantId(tenantId)
                .build();

        Course savedCourse = courseRepository.save(newCourse);
        log.info("Curso salvo com sucesso! ID: {}", savedCourse.getId());

        return savedCourse;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findById(UUID courseId) {
        log.info("Buscando curso pelo ID: {}", courseId);
        return courseRepository.findById(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        log.info("Buscando todos os cursos");
        return courseRepository.findAll();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @courseSecurity.isInstructorOfCourse(authentication.principal, #courseId)")
    public Course updateCourse(UUID courseId, UpdateCourseRequest request, Jwt jwt) {
        log.info("Utilizador {} a tentar atualizar o curso {}", jwt.getSubject(), courseId);

        Course existingCourse = findCourseById(courseId);

        existingCourse.setTitle(request.getTitle());
        existingCourse.setDescription(request.getDescription());

        Course updatedCourse = courseRepository.save(existingCourse);
        log.info("Curso atualizado com sucesso! ID: {}", updatedCourse.getId());
        return updatedCourse;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @courseSecurity.isInstructorOfCourse(authentication.principal, #courseId)")
    public void deleteCourse(UUID courseId, Jwt jwt) {
        log.info("Utilizador {} a tentar apagar o curso {}", jwt.getSubject(), courseId);

        courseRepository.deleteById(courseId);
        log.info("Curso apagado com sucesso! ID: {}", courseId);
    }

    private Course findCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com o ID: " + courseId));
    }
}
