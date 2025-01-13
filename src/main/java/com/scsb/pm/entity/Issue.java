package com.scsb.pm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


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

//    @Lob
//    private byte[] attachment;
    @Column(length = 255, nullable = true)
    private String attachmentName;
//    private String attachmentType;
    private String attachmentPath; // 存SFTP路徑

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEditedAt;

}
