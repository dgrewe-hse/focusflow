/*
 * Copyright (c) 2025 FocusFlow Project Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.hse.focusflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task extends BaseEntity {

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank
    @Size(max = 200)
    @Column(length = 200)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String longDescription;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigneeId")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdById", nullable = false)
    private User createdBy;

    @ManyToMany
    @JoinTable(name = "task_tags", joinColumns = @JoinColumn(name = "taskId"), inverseJoinColumns = @JoinColumn(name = "tagId"))
    private Set<Tag> tags = new HashSet<>();
}
