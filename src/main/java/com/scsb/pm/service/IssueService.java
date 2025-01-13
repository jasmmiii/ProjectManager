package com.scsb.pm.service;

import com.jcraft.jsch.SftpException;
import com.scsb.pm.dao.IssueRepository;
import com.scsb.pm.entity.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface IssueService {

    List<Issue> getIssuesByAssignee(String assignee);

    List<Issue> getAllIssues();

    Issue findById(Long id);

    Issue createIssue(String title, String description, String due, String assignee, String reporter, MultipartFile attachment) throws IOException, SftpException;

    String getAttachmentName(Long issueId);

    Issue getIssueById(Long id);

//    byte[] downloadAttachment(Long id);

    Issue updateIssueStatus(Long id, String status);

    Issue editIssue(Long id, Issue updatedIssue, MultipartFile newAttachment, boolean removeAttachment) throws IOException, SftpException;

    void deleteIssue(Long id);


//    @Autowired
//    private IssueRepository issueRepository;
////    private CommentRepository commentRepository;
//
//
//    /** 抓被指派者的issue for table */
//    public List<Issue> getIssuesByAssignee(String assignee){
//        return issueRepository.findByAssignee(assignee);
//    }
//
//    /** 抓 All issues */
//    public List<Issue> getAllIssues(){
//        return issueRepository.findAll();
//    }
//
//    /** 新增issue */
////    public Issue createIssue(Issue issue){
////        return issueRepository.save(issue);
////    }
//    public Issue createIssue(String title, String description, String due, String assignee, String reporter, MultipartFile attachment) throws IOException {
//        Issue issue = new Issue();
//        issue.setTitle(title);
//        issue.setDescription(description);
//        issue.setDue(due);
//        issue.setAssignee(assignee);
//        issue.setReporter(reporter);
//
//        if (attachment != null && !attachment.isEmpty()) {
//            issue.setAttachment(attachment.getBytes()); // 保存附件的二進制數據
//            issue.setAttachmentType(attachment.getContentType()); // 類型
//            issue.setAttachmentName(attachment.getOriginalFilename()); // 原始文件名
//        }
//        return issueRepository.save(issue);
//    }
//    /** 找附件 */
//    public ResponseEntity<byte[]> getAttachmentResponse(Long id) {
//        Optional<Issue> issueOptional = issueRepository.findById(id);
//        if (issueOptional.isPresent()) { // 判斷是否找到問題
//            Issue issue = issueOptional.get();
//            byte[] attachment = issue.getAttachment();
//            if (attachment != null) {
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.parseMediaType(issue.getAttachmentType())); // 設定Content-Type內容類型
//                headers.setContentDisposition(ContentDisposition.attachment() // 設定Content-Disposition內容處置讓瀏覽器下載文件
//                        .filename(issue.getAttachmentName())
//                        .build());
//                return new ResponseEntity<>(attachment, headers, HttpStatus.OK);
//            }
//        }
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }
//
//
//    /** 使用者更新issue status*/
//    public Issue updateIssueStatus(Long id, String status){
//        Issue issue = issueRepository.findById(id).orElseThrow(()-> new RuntimeException("Error"));
//        issue.setStatus(status);
//        return issueRepository.save(issue);
//    }
//
//    /** 修改並更新issue */
//    public Issue editIssue(Long id, Issue updatedIssue, MultipartFile newAttachment, boolean removeAttachment) throws IOException {
//        Issue issue = issueRepository.findById(id).orElseThrow(() -> new RuntimeException("Update Issue failed"));
//
//        updateEditIssue(issue, updatedIssue);
//
//        // 刪除附件
//        if (removeAttachment) {
//            issue.setAttachment(null);
//            issue.setAttachmentName(null);
//            issue.setAttachmentType(null);
//        } else if (newAttachment != null) {
//            // 更新附件
//            issue.setAttachment(newAttachment.getBytes());
//            issue.setAttachmentName(newAttachment.getOriginalFilename());
//            issue.setAttachmentType(newAttachment.getContentType());
//        }
//        issue.setLastEditedAt(LocalDateTime.now());
//        return issueRepository.save(issue);  // 如果既沒有刪除也沒有上傳新附件，則保持附件不變
//
//    }
//    public void updateEditIssue(Issue issue, Issue updatedIssue){
//        issue.setTitle(updatedIssue.getTitle());
//        issue.setDescription(updatedIssue.getDescription());
//        issue.setStatus(updatedIssue.getStatus());
//        issue.setDue(updatedIssue.getDue());
//        issue.setAssignee(updatedIssue.getAssignee());
//    }
//
//
//    /** 刪除issue */
//    public void deleteIssue(Long id){
//        issueRepository.deleteById(id);
//    }


//    /** 新增comment */
//    public Comment addComment(Long id, String content, String createdBy){
//        Issue issue = issueRepository.findById(id).orElseThrow(()-> new RuntimeException("Issue not found"));
//        Comment comment = new Comment();
//        comment.setIssue(issue);
//        comment.setContent(content);
//        comment.setCreatedBy(createdBy);
//        comment.setCreatedAt(LocalDateTime.now());
//
//        return commentRepository.save(comment);
//    }
//    // 獲取指定 Issue 的所有評論
//    public List<Comment> getCommentsByIssueId(Long id) {
//        return commentRepository.findByIssueId(id);
//    }

}
