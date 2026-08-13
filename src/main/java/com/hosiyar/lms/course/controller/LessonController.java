package com.hosiyar.lms.course.controller;

import com.hosiyar.lms.common.dto.ApiResponse;
import com.hosiyar.lms.common.security.AuthenticatedUser;
import com.hosiyar.lms.course.dto.LessonRequest;
import com.hosiyar.lms.course.dto.LessonResponse;
import com.hosiyar.lms.course.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Lessons are a nested resource - they only exist inside a course, and the
 * URL says so. Every write here is gated by ownership of the parent course.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonResponse>>> list(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        UUID callerId = caller != null ? caller.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(lessonService.findByCourse(courseId, callerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LessonResponse>> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        LessonResponse response = lessonService.create(courseId, request, caller.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lesson created"));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> update(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        LessonResponse response = lessonService.update(courseId, lessonId, request, caller.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson updated"));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        lessonService.delete(courseId, lessonId, caller.getId());
        return ResponseEntity.noContent().build();
    }
}
