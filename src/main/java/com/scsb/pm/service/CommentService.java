//package com.scsb.pm.service;
//
//import com.scsb.pm.dao.CommentRepository;
//import com.scsb.pm.dao.IssueRepository;
//import com.scsb.pm.entity.Comment;
//import com.scsb.pm.entity.Issue;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class CommentService {
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Autowired
//    private IssueRepository issueRepository;
//
//    public List<Comment> getCommentsByIssueId(Long id){
//        return commentRepository.findByIssueId(id);
//    }
//
//    public Comment addComment(Long issueId, Comment comment){
//        Issue issue = issueRepository.findById(issueId)
//                .orElseThrow(() -> new RuntimeException("Issue not found"));
//        comment.setIssue(issue);
//        comment.setCreatedAt(LocalDateTime.now());
//        return commentRepository.save(comment);
//    }
//}
