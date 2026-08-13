package com.hosiyar.lms.course.service;

import com.hosiyar.lms.common.exception.ResourceNotFoundException;
import com.hosiyar.lms.course.dto.LessonRequest;
import com.hosiyar.lms.course.dto.LessonResponse;
import com.hosiyar.lms.course.entity.Course;
import com.hosiyar.lms.course.entity.CourseStatus;
import com.hosiyar.lms.course.entity.Lesson;
import com.hosiyar.lms.course.repository.CourseRepository;
import com.hosiyar.lms.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Every write here answers the same question as CourseService's writes, just
 * one level up the tree: you may touch a lesson only if you own the course
 * it belongs to. A lesson has no separate owner of its own.
 */
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;

    @Transactional
    public LessonResponse create(UUID courseId, LessonRequest request, UUID callerId) {
        Course course = courseService.requireOwnedCourse(courseId, callerId);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setPosition(request.position());

        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse update(UUID courseId, UUID lessonId, LessonRequest request, UUID callerId) {
        courseService.requireOwnedCourse(courseId, callerId);

        Lesson lesson = requireLessonInCourse(lessonId, courseId);
        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setPosition(request.position());

        return toResponse(lesson);
    }

    @Transactional
    public void delete(UUID courseId, UUID lessonId, UUID callerId) {
        courseService.requireOwnedCourse(courseId, callerId);
        lessonRepository.delete(requireLessonInCourse(lessonId, courseId));
    }

    /**
     * Reading lessons follows the same visibility rule as the course itself:
     * published is public, a draft's lessons are the owner's business only.
     * callerId is null for anonymous visitors.
     */
    @Transactional(readOnly = true)
    public List<LessonResponse> findByCourse(UUID courseId, UUID callerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean isPublished = course.getStatus() == CourseStatus.PUBLISHED;
        boolean isOwner = callerId != null && course.isOwnedBy(callerId);

        if (!isPublished && !isOwner) {
            throw new ResourceNotFoundException("Course not found");
        }

        return lessonRepository.findAllByCourseIdOrderByPositionAsc(courseId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Note this looks up by lesson id AND course id together.
     *
     * Querying by lesson id alone would let someone pass a lesson belonging
     * to course B through course A's path - passing the ownership check on
     * A while actually editing something in B.
     */
    private Lesson requireLessonInCourse(UUID lessonId, UUID courseId) {
        return lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }

    private LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getCourse().getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getPosition(),
                lesson.getFileKey(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
