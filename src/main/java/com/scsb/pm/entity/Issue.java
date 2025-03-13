package com.scsb.pm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="issue")
@Getter
@Setter
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status = "ToDo";
    private String due;
    private String assignee;
    private String reporter;

    @Column(length = 255, nullable = true)
    private String attachmentName;
    private String attachmentPath; // 存SFTP路徑

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEditedAt;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference // 避免循環 讓Issue 反向參考Project
    private Project project;

    // soft delete
    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();
}
