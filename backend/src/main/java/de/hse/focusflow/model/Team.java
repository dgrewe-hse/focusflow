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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Team entity in the application.
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team extends BaseEntity {

    /**
     * The team's name
     */
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The team's description
     */
    @Size(max = 255)
    @Column(length = 255)
    private String description;

    /**
     * The team lead/manager
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamLeadId", nullable = false)
    private User teamLead;

    /**
     * Tasks assigned to this team
     */
    @OneToMany(mappedBy = "team")
    private Set<Task> tasks = new HashSet<>();

    /**
     * Members of this team
     */
    @ManyToMany
    @JoinTable(name = "team_members", joinColumns = @JoinColumn(name = "teamId"), inverseJoinColumns = @JoinColumn(name = "userId"))
    private Set<User> members = new HashSet<>();
}
