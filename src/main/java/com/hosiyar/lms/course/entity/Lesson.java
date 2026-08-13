package com.hosiyar.lms.course.entity;

import com.hosiyar.lms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contrast this with Course.instructorId deliberately.
 *
 * Lesson and Course both live in the course module, so an ordinary
 * @ManyToOne with a real foreign key is exactly right here. ADR-006 was
 * about crossing module boundaries, not about avoiding relationships
 * everywhere - inside a module, use JPA normally.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Explicit ordering. Without this, lesson order would depend on whatever
     * the database happened to return - JPA guarantees no ordering otherwise.
     */
    @Column(nullable = false)
    private Integer position;

    /**
     * Set in chapter 4, when file upload arrives. Null until then.
     */
    @Column(name = "file_key")
    private String fileKey;
}
