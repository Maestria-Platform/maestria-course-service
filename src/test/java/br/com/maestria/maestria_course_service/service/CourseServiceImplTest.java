package br.com.maestria.maestria_course_service.service;

import br.com.maestria.maestria_course_service.dto.request.CreateCourseRequest;
import br.com.maestria.maestria_course_service.dto.request.UpdateCourseRequest;
import br.com.maestria.maestria_course_service.entity.Course;
import br.com.maestria.maestria_course_service.exception.ResourceNotFoundException;
import br.com.maestria.maestria_course_service.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Jwt mockJwt;
    private UUID instructorId;
    private UUID tenantId;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        instructorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(instructorId.toString())
                .claim("tenantId", tenantId.toString())
                .build();
    }

    @Nested
    @DisplayName("Testes para criação de cursos")
    class CreateCourseTests {
        @Test
        @DisplayName("Deve salvar e retornar o curso quando os dados são válidos")
        void createCourse_ShouldSaveAndReturnCourse() {
            CreateCourseRequest request = new CreateCourseRequest();
            request.setTitle("Novo Curso de Java");
            request.setDescription("Descrição do curso de Java");

            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course result = courseService.createCourse(request, mockJwt);

            assertNotNull(result);
            assertEquals(request.getTitle(), result.getTitle());
            assertEquals(instructorId, result.getInstructorId());
            assertEquals(tenantId, result.getTenantId());
            verify(courseRepository, times(1)).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Testes para busca de cursos")
    class FindCourseTests {
        @Test
        @DisplayName("Deve retornar um Optional com o curso quando o ID existe")
        void findById_WhenCourseExists_ShouldReturnOptionalOfCourse() {
            Course course = new Course();
            course.setId(courseId);
            OngoingStubbing<Optional<Course>> optionalOngoingStubbing = when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

            Optional<Course> result = courseService.findById(courseId);

            assertTrue(result.isPresent());
            assertEquals(courseId, result.get().getId());
            verify(courseRepository, times(1)).findById(courseId);
        }

        @Test
        @DisplayName("Deve retornar um Optional vazio quando o ID não existe")
        void findById_WhenCourseDoesNotExist_ShouldReturnEmptyOptional() {
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            Optional<Course> result = courseService.findById(courseId);

            assertFalse(result.isPresent());
            verify(courseRepository, times(1)).findById(courseId);
        }

        @Test
        @DisplayName("Deve retornar uma lista de cursos quando existem cursos")
        void findAll_WhenCoursesExist_ShouldReturnCourseList() {
            List<Course> courses = Arrays.asList(new Course(), new Course());
            when(courseRepository.findAll()).thenReturn(courses);

            List<Course> result = courseService.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(courseRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia quando não existem cursos")
        void findAll_WhenNoCoursesExist_ShouldReturnEmptyList() {
            when(courseRepository.findAll()).thenReturn(Collections.emptyList());

            List<Course> result = courseService.findAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(courseRepository, times(1)).findAll();
        }
    }


    @Nested
    @DisplayName("Testes para atualização de cursos")
    class UpdateCourseTests {
        @Test
        @DisplayName("Deve atualizar e retornar o curso quando o ID existe")
        void updateCourse_WhenCourseExists_ShouldUpdateAndReturnCourse() {
            UpdateCourseRequest request = new UpdateCourseRequest();
            request.setTitle("Curso Atualizado");
            request.setDescription("Nova descrição");

            Course existingCourse = Course.builder().id(courseId).title("Título Antigo").description("Descrição Antiga").build();
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updatedCourse = courseService.updateCourse(courseId, request, mockJwt);

            assertNotNull(updatedCourse);
            assertEquals(request.getTitle(), updatedCourse.getTitle());
            assertEquals(request.getDescription(), updatedCourse.getDescription());
            verify(courseRepository, times(1)).findById(courseId);
            verify(courseRepository, times(1)).save(existingCourse);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar um curso que não existe")
        void updateCourse_WhenCourseDoesNotExist_ShouldThrowResourceNotFoundException() {
            UpdateCourseRequest request = new UpdateCourseRequest();
            request.setTitle("Curso Inexistente");
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                courseService.updateCourse(courseId, request, mockJwt);
            });

            verify(courseRepository, times(1)).findById(courseId);
            verify(courseRepository, never()).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Testes para exclusão de cursos")
    class DeleteCourseTests {

        @Test
        @DisplayName("Deve chamar o método deleteById do repositório ao deletar um curso")
        void deleteCourse_WhenCourseExists_ShouldCallDeleteById() {
            courseService.deleteCourse(courseId, mockJwt);
            verify(courseRepository, times(1)).deleteById(courseId);
        }
    }
}