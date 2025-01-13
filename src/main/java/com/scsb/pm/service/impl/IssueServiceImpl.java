package com.scsb.pm.service.impl;

import com.jcraft.jsch.SftpException;
import com.scsb.pm.dao.IssueRepository;
import com.scsb.pm.entity.Issue;
import com.scsb.pm.service.IssueService;
import com.scsb.pm.service.helper.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    @Autowired
    private SftpService sftpService;

    public IssueServiceImpl(IssueRepository issueRepository, SftpService sftpService) {
        this.issueRepository = issueRepository;
        this.sftpService = sftpService;
    }

    @Override
    public List<Issue> getIssuesByAssignee(String assignee){
        return issueRepository.findByAssignee(assignee);
    }

    @Override
    public List<Issue> getAllIssues(){
        return issueRepository.findAll();
    }

    @Override
    public Issue findById(Long id) {
        return issueRepository.findById(id).orElse(null);
    }


    @Override
    public Issue createIssue(String title, String description, String due, String assignee, String reporter, MultipartFile attachment) throws IOException, SftpException {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setDue(due);
        issue.setAssignee(assignee);
        issue.setReporter(reporter);

        // SFTP 上傳檔案
        if (attachment != null && !attachment.isEmpty()) {
            try {
                String fileName = attachment.getOriginalFilename();
                InputStream fileStream = attachment.getInputStream();
                String uploadedPath = sftpService.uploadFile(fileStream, fileName);

                // 保存檔案路徑、名稱、時間到資料庫
                issue.setAttachmentPath(uploadedPath);
                issue.setAttachmentName(fileName);
                issue.setLastEditedAt(LocalDateTime.now());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return issueRepository.save(issue);
    }
    @Override
    public String getAttachmentName(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));
        return issue.getAttachmentName(); // 返回附件名稱
    }


    @Override
    public Issue getIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found for ID: " + id));
    }
//    @Override
//    public byte[] downloadAttachment(Long id) {
//        Issue issue = getIssueById(id);
//        String remoteFilePath = issue.getAttachmentPath();
//
//        if (remoteFilePath == null || remoteFilePath.isEmpty()) {
//            throw new RuntimeException("No attachment found for issue ID: " + id);
//        }
//        System.out.println("Attempting to download file from path:" + remoteFilePath);
//
//        try {
//            SftpService sftpService = new SftpService();
//            return sftpService.downloadFile(remoteFilePath);
//        } catch (Exception e) {
//            System.err.println("Failed to download file from path: " + remoteFilePath);
//            throw new RuntimeException("Failed to download attachment for issue ID: " + id, e);
//        }
//    }


    /** 原 檔案存資料庫方法
    @Override
    public ResponseEntity<byte[]> getAttachmentResponse(Long id) {
        Optional<Issue> issueOptional = issueRepository.findById(id);
        if (issueOptional.isPresent()) { // 判斷是否找到問題
            Issue issue = issueOptional.get();
            byte[] attachment = issue.getAttachment();
            if (attachment != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(issue.getAttachmentType())); // 設定Content-Type內容類型
                headers.setContentDisposition(ContentDisposition.attachment() // 設定Content-Disposition內容處置讓瀏覽器下載文件
                        .filename(issue.getAttachmentName())
                        .build());
                return new ResponseEntity<>(attachment, headers, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }*/

    @Override
    public Issue updateIssueStatus(Long id, String status) {
        Issue issue = issueRepository.findById(id).orElseThrow(()-> new RuntimeException("Error"));
        issue.setStatus(status);
        return issueRepository.save(issue);
    }

    @Override
    public Issue editIssue(Long id, Issue updatedIssue, MultipartFile newAttachment, boolean removeAttachment) throws IOException, SftpException {
        Issue issue = issueRepository.findById(id).orElseThrow(() -> new RuntimeException("Update Issue failed for ID: " + id));

        issue.setTitle(updatedIssue.getTitle());
        issue.setDescription(updatedIssue.getDescription());
        issue.setStatus(updatedIssue.getStatus());
        issue.setDue(updatedIssue.getDue());
        issue.setAssignee(updatedIssue.getAssignee());


        // 刪除附件
        if (removeAttachment) {
            try {
//                if (issue.getAttachmentPath() != null && !issue.getAttachmentPath().isEmpty()) {
//                    sftpService.deleteFile(issue.getAttachmentPath());
//                }
                issue.setAttachmentPath(null);
                issue.setAttachmentName(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete attachment for issue ID: " + id, e);
            }

        // 更新附件
        } else if (newAttachment != null && !newAttachment.isEmpty()) {
            try{
                String fileName = newAttachment.getOriginalFilename();
                InputStream fileStream = newAttachment.getInputStream();
                String uploadPath = sftpService.uploadFile(fileStream, fileName);

                issue.setAttachmentPath(uploadPath);
                issue.setAttachmentName(fileName);
            } catch (Exception e){
                throw new RuntimeException("Failed to upload new attachment for issue ID: " + id, e);
            }

        }
        issue.setLastEditedAt(LocalDateTime.now());
        return issueRepository.save(issue);  // 如果既沒有刪除也沒有上傳新附件，則保持附件不變
    }

    @Override
    public void deleteIssue(Long id) {
        issueRepository.deleteById(id);
    }
}
