package com.hosiyar.lms.course.repository;

import com.hosiyar.lms.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findAllByCourseIdOrderByPositionAsc(UUID courseId);

    /**
     * Looks a lesson up by BOTH its own id and its parent course id.
     *
     * That second condition matters: it stops a lesson id from one course
     * being operated on through a different course's URL path, which would
     * sidestep the ownership check on the course the caller actually owns.
     */
    Optional<Lesson> findByIdAndCourseId(UUID id, UUID courseId);
}
