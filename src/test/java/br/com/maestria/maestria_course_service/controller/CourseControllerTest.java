package br.com.maestria.maestria_course_service.controller;


import br.com.maestria.maestria_course_service.dto.request.CreateCourseRequest;
import br.com.maestria.maestria_course_service.dto.request.UpdateCourseRequest;
import br.com.maestria.maestria_course_service.entity.Course;
import br.com.maestria.maestria_course_service.repository.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
    }

    private JwtGrantedAuthoritiesConverter authoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("ROLE_");
        converter.setAuthoritiesClaimName("roles");
        return converter;
    }
    @Nested
    @DisplayName("POST /api/v1/courses - Criação de Cursos")
    class CreateCourseTests {

        @Test
        @DisplayName("Deve retornar 201 Created quando o usuário for ADMIN")
        void createCourse_WhenUserIsAdmin_ShouldReturn201Created() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest();
            request.setTitle("Curso de Teste de Integração");
            request.setDescription("Descrição do teste");
            request.setPrice(new BigDecimal("99.90"));

            mockMvc.perform(post("/api/v1/courses")
                            .with(jwt().jwt(j -> j
                                                    .subject(UUID.randomUUID().toString())
                                                    .claim("tenantId", UUID.randomUUID().toString())
                                                    .claim("roles", Collections.singletonList("ADMIN"))
                                            )
                                            .authorities(authoritiesConverter())
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.title").value("Curso de Teste de Integração"))
                    .andExpect(jsonPath("$.price").value(99.90));;

        }

        @Test
        @WithMockUser(roles = "ALUNO")
        @DisplayName("Deve retornar 403 Forbidden quando o usuário for ALUNO")
        void createCourse_WhenUserIsAluno_ShouldReturn403Forbidden() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest();
            request.setTitle("Tentativa de Criação por Aluno");

            mockMvc.perform(post("/api/v1/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/courses - Listagem de Cursos")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Deve retornar 200 OK e a lista de cursos")
        void getAllCourses_ShouldReturn200OkAndCourseList() throws Exception {
            Course course = Course.builder()
                    .title("Curso Público")
                    .description("Descrição pública")
                    .price(new BigDecimal("49.99"))
                    .instructorId(UUID.randomUUID())
                    .tenantId(UUID.randomUUID())
                    .build();
            courseRepository.save(course);

            mockMvc.perform(get("/api/v1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Curso Público"))
                    .andExpect(jsonPath("$[0].price").value(49.99));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/courses - Cenários Adicionais de Criação")
    class CreateCourseExtraScenariosTests {

        @Test
        @DisplayName("Deve retornar 201 Created quando o usuário for INSTRUTOR")
        void createCourse_WhenUserIsInstructor_ShouldReturn201Created() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest();
            request.setTitle("Curso Criado por Instrutor");
            request.setDescription("Descrição do instrutor");
            request.setPrice(new BigDecimal("50.00"));

            mockMvc.perform(post("/api/v1/courses")
                            .with(jwt().jwt(j -> j
                                                    .subject(UUID.randomUUID().toString())
                                                    .claim("tenantId", UUID.randomUUID().toString())
                                                    .claim("roles", Collections.singletonList("INSTRUTOR"))
                                            )
                                            .authorities(authoritiesConverter())
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Curso Criado por Instrutor"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/courses/{id} - Busca por ID")
    class GetCourseByIdTests {

        @Test
        @DisplayName("Deve retornar 200 OK e o curso quando o ID existe")
        void getCourseById_WhenCourseExists_ShouldReturn200Ok() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Curso por ID")
                    .description("Desc")
                    .price(new BigDecimal("49.99"))
                    .instructorId(UUID.randomUUID())
                    .tenantId(UUID.randomUUID())
                    .build());

            mockMvc.perform(get("/api/v1/courses/{id}", course.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(course.getId().toString()))
                    .andExpect(jsonPath("$.title").value("Curso por ID"))
                    .andExpect(jsonPath("$.price").value(49.99));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o ID não existe")
        void getCourseById_WhenCourseDoesNotExist_ShouldReturn404NotFound() throws Exception {
            mockMvc.perform(get("/api/v1/courses/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/courses/{id} - Atualização de Cursos")
    class UpdateCourseTests {

        private final UUID ownerInstructorId = UUID.randomUUID();
        private final UUID otherInstructorId = UUID.randomUUID();
        private final UUID tenantId = UUID.randomUUID();

        @Test
        @DisplayName("Deve retornar 200 OK quando o INSTRUTOR proprietário atualiza o curso")
        void updateCourse_WhenOwnerInstructor_ShouldReturn200Ok() throws Exception {

            Course course = courseRepository.save(Course.builder()
                    .title("Original")
                    .description("Desc")
                    .price(new BigDecimal("10.00"))
                    .instructorId(ownerInstructorId)
                    .tenantId(tenantId)
                    .build());

            UpdateCourseRequest request = new UpdateCourseRequest();
            request.setTitle("Atualizado pelo Dono");
            request.setDescription("Nova desc");
            request.setPrice(new BigDecimal("12.50"));

            mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(ownerInstructorId.toString()).claim("roles", Collections.singletonList("INSTRUTOR")))
                                    .authorities(authoritiesConverter())
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Atualizado pelo Dono"))
                    .andExpect(jsonPath("$.price").value(12.50));
        }

        @Test
        @DisplayName("Deve retornar 200 OK quando o ADMIN atualiza um curso que não lhe pertence")
        void updateCourse_WhenAdmin_ShouldReturn200Ok() throws Exception {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("Original")
                            .description("Desc")
                            .price(new BigDecimal("20.00"))
                            .instructorId(ownerInstructorId)
                            .tenantId(tenantId)
                            .build()
            );

            UpdateCourseRequest request = new UpdateCourseRequest();
            request.setTitle("Atualizado pelo Admin");
            request.setDescription("Nova desc");
            request.setPrice(new BigDecimal("22.50"));

            mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("roles", Collections.singletonList("ADMIN")))
                                    .authorities(authoritiesConverter())
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Atualizado pelo Admin"));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden quando um INSTRUTOR tenta atualizar um curso que não lhe pertence")
        void updateCourse_WhenNotOwnerInstructor_ShouldReturn403Forbidden() throws Exception {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("Original")
                            .description("Desc")
                            .price(new BigDecimal("30.00"))
                            .instructorId(ownerInstructorId)
                            .tenantId(tenantId)
                            .build()
            );
            UpdateCourseRequest request = new UpdateCourseRequest();
            request.setTitle("Tentativa de fraude");
            request.setPrice(new BigDecimal("0.00"));

            mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(otherInstructorId.toString()).claim("roles", Collections.singletonList("INSTRUTOR")))
                                    .authorities(authoritiesConverter())
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/courses/{id} - Exclusão de Cursos")
    class DeleteCourseTests {

        private final UUID ownerInstructorId = UUID.randomUUID();
        private final UUID otherInstructorId = UUID.randomUUID();
        private final UUID tenantId = UUID.randomUUID();

        @Test
        @DisplayName("Deve retornar 204 No Content quando o INSTRUTOR proprietário exclui o curso")
        void deleteCourse_WhenOwnerInstructor_ShouldReturn204NoContent() throws Exception {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("A ser deletado")
                            .description("Desc")
                            .price(new BigDecimal("1.00"))
                            .instructorId(ownerInstructorId)
                            .tenantId(tenantId)
                            .build());

            mockMvc.perform(delete("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(ownerInstructorId.toString()).claim("roles", Collections.singletonList("INSTRUTOR")))
                                    .authorities(authoritiesConverter())
                            ))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 204 No Content quando o ADMIN exclui um curso que não lhe pertence")
        void deleteCourse_WhenAdmin_ShouldReturn204NoContent() throws Exception {

            Course course = courseRepository.save(
                    Course.builder()
                            .title("A ser deletado")
                            .description("Desc")
                            .price(new BigDecimal("1.00"))
                            .instructorId(ownerInstructorId)
                            .tenantId(tenantId)
                            .build()
            );

            mockMvc.perform(delete("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("roles", Collections.singletonList("ADMIN")))
                                    .authorities(authoritiesConverter())
                            ))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden quando um INSTRUTOR tenta excluir um curso que não lhe pertence")
        void deleteCourse_WhenNotOwnerInstructor_ShouldReturn403Forbidden() throws Exception {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("A ser deletado")
                            .description("Desc")
                            .price(new BigDecimal("3.00"))
                            .instructorId(ownerInstructorId)
                            .tenantId(tenantId)
                            .build()
            );

            mockMvc.perform(delete("/api/v1/courses/{id}", course.getId())
                            .with(jwt().jwt(j -> j.subject(otherInstructorId.toString()).claim("roles", Collections.singletonList("INSTRUTOR")))
                                    .authorities(authoritiesConverter())
                            ))
                    .andExpect(status().isForbidden());
        }
    }
}