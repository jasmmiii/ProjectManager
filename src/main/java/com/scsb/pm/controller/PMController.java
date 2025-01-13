package com.scsb.pm.controller;

import com.jcraft.jsch.SftpException;
import com.scsb.pm.dao.IssueRepository;
import com.scsb.pm.entity.Issue;
import com.scsb.pm.service.IssueService;
import com.scsb.pm.service.LoginService;
import com.scsb.pm.service.helper.SftpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
public class PMController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private IssueService issueService;

    private final SftpService sftpService = new SftpService();
    @Autowired
    private IssueRepository issueRepository;


    /** Show login page */
    @GetMapping("/")
    public String showLoginForm() {
        return "login";
    }

    /** 驗證碼 */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        loginService.generateCaptcha(request, response);
    }

    /** Handle login */
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> handleLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("captcha") String captcha,
            HttpSession session,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        if(!loginService.validateCaptcha(request, captcha)) {
            response.put("success", false);
            response.put("message", "Captcha verification failed");
            return response;
        }
        loginService.clearCaptcha(request);

        // 用戶驗證
        boolean isMember = loginService.verify(username, password);
        if (isMember) {
            String role = loginService.getRole(username); //如果是用戶再獲取role
            response.put("success", true);
            response.put("username", loginService.escapeHtml(username)); //在Service進行轉義 把用戶名傳到前端
            response.put("role", loginService.escapeHtml(role));

            session.setAttribute("username", loginService.escapeHtml(username));
            session.setAttribute("role", loginService.escapeHtml(role));
            return response;
        } else {
            response.put("success", false);
            response.put("message", "Username or Password not found");
            return response;
        }
    }

    /** Show homepage */
    @GetMapping ("/homepage")
    public String showHomepage(){
        return "homepage";
    }

    /** Log out */
    @GetMapping("/logout")
    public String logout(){ return "login"; }


    /** Get issues for THE USER */
    @GetMapping("/api/issues")
    @ResponseBody
    public List<Issue> getMyIssues(HttpSession session){
        String username = (String) session.getAttribute("username");
        return issueService.getIssuesByAssignee(username);
    }

    /** Get All issues */
    @GetMapping("/api/issues/all")
    @ResponseBody
    public List<Issue> getAllIssues(){
        return issueService.getAllIssues();
    }

    /** Create new issue */
    @PostMapping("/api/issues")
    @ResponseBody
//    public Issue createIssue(@RequestBody Issue issue){
//        return issueService.createIssue(issue);
//    }
//    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) {
//        Issue createdIssue = issueService.createIssue(issue);
//        return ResponseEntity.ok(createdIssue);
//    }
    public ResponseEntity<Issue> createIssue(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("due") String due,
            @RequestParam("assignee") String assignee,
            @RequestParam("reporter") String reporter,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) {

        try {
            Issue createdIssue = issueService.createIssue(title, description, due, assignee, reporter, attachment);
            return ResponseEntity.ok(createdIssue);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /** Get attachment */
//    @GetMapping("/api/issues/{id}/attachment")
//    public ResponseEntity<byte[]> getAttachment(@PathVariable Long id) {
//        return issueService.getAttachmentResponse(id);
//    }
    /** Download Attachment
    @GetMapping("/api/issues/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        try {
            byte[] fileData = issueService.downloadAttachment(id);
            Optional<Issue> issueOptional = issueRepository.findById(id); // 設置下載檔案的 Content-Disposition
            HttpHeaders headers = new HttpHeaders();

            if (issueOptional.isPresent()) {
                String fileName = issueOptional.get().getAttachmentName();
                headers.setContentDispositionFormData("attachment", fileName); // 強制下載
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // 設置檔案類型
                return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/





    /** Update issue status */
    @PutMapping("/api/issues/{id}/status")
    @ResponseBody
    public Issue updateIssueStatus(@PathVariable Long id, @RequestBody Map<String, String> request){
        String status = request.get("status");
        return issueService.updateIssueStatus(id, status);
    }

    /**
     * Edit issue
     * */
//    @GetMapping("/api/issues/{id}")
//    public ResponseEntity<Issue> getIssue(@PathVariable Long id) {
//        Issue issue = issueRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Issue not found"));
//        return ResponseEntity.ok(issue);
//    }
    @PutMapping("/api/issues/{id}")
    @ResponseBody
    public ResponseEntity<Issue> editIssue(
            @PathVariable Long id,
            @RequestPart("issue") Issue updatedIssue,
            @RequestPart(value = "attachment", required = false) MultipartFile newAttachment,
            @RequestParam(value = "removeAttachment", defaultValue = "false") boolean removeAttachment
    ){
        try {
            Issue updated = issueService.editIssue(id, updatedIssue, newAttachment, removeAttachment);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }


    /** Delete issue */
    @DeleteMapping("/api/issues/{id}")
    @ResponseBody
    public void deleteIssue(@PathVariable Long id){
        issueService.deleteIssue(id);
    }


//    /** 新增Comment */
//    @PostMapping("/api/issues/{id}/comments")
//    @ResponseBody
//    public ResponseEntity<Comment> addComment(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> payload){
//        String content = payload.get("content");
//        String createdBy = "currentUsername";
//        Comment comment = issueService.addComment(id, content, createdBy);
//        return ResponseEntity.ok(comment);
//    }
//
//    // 獲取指定 Issue 的所有評論
//    @GetMapping("/api/issues/{id}/comments")
//    public ResponseEntity<List<Comment>> getCommentsByIssueId(@PathVariable Long id) {
//        List<Comment> comments = issueService.getCommentsByIssueId(id);
//        return ResponseEntity.ok(comments);
//    }
}

