/**
 * Course catalogue, lessons and content.
 *
 * Course creation and catalogue reads implemented (chapter 2). Ownership-based
 * update/delete and lessons land in chapter 3; file upload in chapter 4.
 *
 * This module references users by id only, through the user module's public
 * UserDirectory interface - never its entity or repository. See ADR-006.
 */
package com.hosiyar.lms.course;
