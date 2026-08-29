package com.hosiyar.lms.course.controller;

import com.hosiyar.lms.course.service.LessonService;
import com.hosiyar.lms.user.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the security gate around lessons. We're testing the gate -
 * routing, roles, content types - not the business logic behind it, which is
 * covered by LessonServiceTest.
 *
 * Maps to docs/prd/module-02-course-management.md US-6 and US-7.
 */
@WebMvcTest(LessonController.class)
@Import({
        com.hosiyar.lms.user.config.SecurityConfig.class,
        com.hosiyar.lms.common.config.CorsConfig.class,
        com.hosiyar.lms.user.security.JwtAuthenticationEntryPoint.class,
        com.hosiyar.lms.user.security.RestAccessDeniedHandler.class
})
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UUID courseId = UUID.randomUUID();
    private final UUID lessonId = UUID.randomUUID();

    @Test
    @DisplayName("uploading a file without a token gives 401")
    void uploadWithoutTokenIsUnauthorized() throws Exception {
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file", "x.pdf", "application/pdf", new byte[16]);

        mockMvc.perform(multipart("/api/v1/courses/{c}/lessons/{l}/file", courseId, lessonId)
                        .file(file))
                .andExpect(status().isUnauthorized());

        verify(lessonService, never()).attachFile(any(), any(), any(), any());
    }

    @Test
    @DisplayName("a student uploading a file gives 403 - wrong role")
    @WithMockUser(roles = "STUDENT")
    void uploadAsStudentIsForbidden() throws Exception {
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file", "x.pdf", "application/pdf", new byte[16]);

        mockMvc.perform(multipart("/api/v1/courses/{c}/lessons/{l}/file", courseId, lessonId)
                        .file(file))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).attachFile(any(), any(), any(), any());
    }

    // Note: the instructor happy-path isn't slice-tested here. @WithMockUser
    // doesn't populate a real AuthenticatedUser principal, so the controller's
    // caller.getId() would NPE - a test artifact, not a real bug. The upload
    // success path is covered end to end in LessonServiceTest instead, which
    // is where the actual logic lives.
}
