//package com.scsb.pm.entity;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//
//@Entity
//@Table(name = "comment")
//@Getter
//@Setter
//public class Comment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "issue_id", nullable = false)
//    private Issue issue;
//
//    @Column(length= 1000, nullable = false)
//    private String content;
//
//    private String createdBy;
//
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createdAt;
//}
