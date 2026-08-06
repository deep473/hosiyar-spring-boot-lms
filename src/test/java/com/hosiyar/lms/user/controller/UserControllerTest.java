package com.hosiyar.lms.user.controller;

import com.hosiyar.lms.user.security.JwtAuthenticationFilter;
import com.hosiyar.lms.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Note @MockitoBean - this replaced @MockBean in Spring Boot 4.
 *
 * Maps back to the acceptance criteria in
 * docs/prd/module-01-users-auth.md (US-4 and US-5).
 */
@WebMvcTest(UserController.class)
@Import({
        com.hosiyar.lms.user.config.SecurityConfig.class,
        com.hosiyar.lms.user.security.JwtAuthenticationEntryPoint.class,
        com.hosiyar.lms.user.security.RestAccessDeniedHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("no token at all gives 401 - we don't know who you are")
    void listUsersWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).findAll();
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("a student token gives 403 - we know who you are, and no")
    void listUsersAsStudentIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("an admin token is allowed through")
    void listUsersAsAdminIsAllowed() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("the profile endpoint also requires authentication")
    void meWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getByEmail(anyString());
    }
}
