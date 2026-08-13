package com.hosiyar.lms.course.dto;

import java.time.Instant;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        UUID courseId,
        String title,
        String content,
        Integer position,
        String fileKey,
        Instant createdAt,
        Instant updatedAt
) {}
