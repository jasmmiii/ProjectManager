package com.scsb.pm.controller;

import com.scsb.pm.dao.CommentRepository;
import com.scsb.pm.dao.IssueRepository;
import com.scsb.pm.dao.LoginRepository;
import com.scsb.pm.dao.ProjectRepository;
import com.scsb.pm.entity.Comment;
import com.scsb.pm.entity.Issue;
import com.scsb.pm.entity.Member;
import com.scsb.pm.entity.Project;
//import com.scsb.pm.service.CommentService;
import com.scsb.pm.service.IssueService;
import com.scsb.pm.service.LoginService;
import com.scsb.pm.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class PMController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private LoginRepository loginRepository;

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




    /** 進入 Project 列表 */
    @GetMapping("/projects")
    public String showProjects() {
        return "homepage";
    }

    /** Get All Projects with filter */
    @GetMapping("/api/projects")
    @ResponseBody
    public List<Project> getProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy){

        if (createdBy != null) {
            createdBy = createdBy.trim(); // 確保去除空格
        }

        List<Project> projects = projectRepository.searchProjects(name, status, createdBy);
        return projects;
    }

    /** Create Project */
    @PostMapping("/api/projects")
    @ResponseBody
    public ResponseEntity<Project> createProject(@RequestBody Project project){
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    // 取得 pm_db 的所有 username (用於前端下拉選單)
    @GetMapping("/api/projects/members")
    @ResponseBody
    public List<Member> getAllMembers() {
        List<Member> members = loginRepository.findAll();
        return members;
    }

    /** 更新 Project Status */
    @PutMapping("/api/projects/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateProjectStatus(@PathVariable Long id, @RequestBody Map<String, String> request){
        String newStatus = request.get("status");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project Not Found"));

        project.setStatus(newStatus);
        projectRepository.save(project);

        return ResponseEntity.ok("Project status updated successfully!");
    }


    /** 進入指定 Project 的 Issues 頁面 */
    @GetMapping("/projects/{id}/issues")
    public String showIssues(@PathVariable Long id, Model model) {
        model.addAttribute("projectId", id); // 傳 projectId 給前端
        return "issue";
    }

    /** 取得指定 project 的所有 Issues */
    @GetMapping("/api/projects/{id}/issues")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProjectIssues(@PathVariable Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        List<Issue> issues = issueRepository.findByProjectAndDeletedFalse(project);

        Map<String, Object> response = new HashMap<>();
        response.put("projectName", project.getName());
        response.put("projectStatus", project.getStatus());
        response.put("projectCreatedBy", project.getCreatedBy());
        response.put("projectCreatedDate", project.getCreatedDate());
        response.put("projectDescription", project.getDescription());
        response.put("issues", issues);

        return ResponseEntity.ok(response);
    }

    /** 進入Project的Issues頁面 更新Project */
    @PutMapping("/api/projects/{id}")
    @ResponseBody
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody Map<String, String> request){

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // 更新狀態
        if (request.containsKey("status")) {
            project.setStatus(request.get("status"));
        }
        // 更新 description
        if (request.containsKey("description")) {
            project.setDescription(request.get("description"));
        }

        projectRepository.save(project);
        return ResponseEntity.ok(project);
    }


//    /** 原 Get issues for THE USER */
//    @GetMapping("/api/issues")
//    @ResponseBody
//    public List<Issue> getMyIssues(HttpSession session){
//        String username = (String) session.getAttribute("username");
//        return issueService.getIssuesByAssignee(username);
//    }
//    /** 原 Get All issues */
//    @GetMapping("/api/issues/all")
//    @ResponseBody
//    public List<Issue> getAllIssues(){
//        return issueService.getAllIssues();
//    }

    /** Create new issue */
    @PostMapping("/api/projects/{id}/issues")
    @ResponseBody
    public ResponseEntity<Issue> createIssue(
            @PathVariable("id") Long projectId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("due") String due,
            @RequestParam("assignee") String assignee,
            @RequestParam("reporter") String reporter,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) {

        try {
            Project project = projectRepository.findById(projectId) // 從資料庫取得 Project
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

            Issue createdIssue = issueService.createIssue(title, description, due, assignee, reporter, attachment, project);
            return ResponseEntity.ok(createdIssue);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
//    /** Get attachment */
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
    @PutMapping("/api/projects/{projectId}/issues/{id}/status")
    @ResponseBody
    public Issue updateIssueStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request){

        String status = request.get("status");
        return issueService.updateIssueStatus(id, status);
    }

    /**
     * Edit issue
     * */
    @PutMapping("/api/projects/{projectId}/issues/{id}")
    @ResponseBody
    public ResponseEntity<Issue> editIssue(
            @PathVariable Long id,
            @PathVariable("projectId") Long projectId,
            @RequestPart("issue") Issue updatedIssue,
            @RequestPart(value = "attachment", required = false) MultipartFile newAttachment,
            @RequestParam(value = "removeAttachment", defaultValue = "false") boolean removeAttachment
    ){
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

            Issue updated = issueService.editIssue(id, updatedIssue, newAttachment, removeAttachment, project);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }


    /** Delete issue */
    @DeleteMapping("/api/projects/{projectId}/issues/{id}")
    @ResponseBody
    public void deleteIssue(@PathVariable Long id){
        issueService.deleteIssue(id);
    }


    /** 進入指定 Issue 的 Detail-Comment 頁面*/
    @GetMapping("/projects/{projectId}/issues/{issueId}")
    public String showIssueDetail(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            HttpSession session,
            Model model) {

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        model.addAttribute("username", username);
        model.addAttribute("role", role);

        model.addAttribute("projectId", projectId);
        model.addAttribute("issueId", issueId);
        return "comment"; // ✅ 回傳 comment.html
    }

    /** 取得指定Issue的詳細資料、留言 */
    @GetMapping("/api/projects/{projectId}/issues/{issueId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getIssueDetails(
            @PathVariable Long projectId,
            @PathVariable Long issueId){

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("projectName", project.getName());
        response.put("id", issue.getId());
        response.put("title", issue.getTitle());
        response.put("description", issue.getDescription());
        response.put("status", issue.getStatus());
        response.put("due", issue.getDue());
        response.put("reporter", issue.getReporter());
        response.put("assignee", issue.getAssignee());

        // 加入留言
        List<Map<String, Object>> comments = issue.getComments().stream().map(comment -> {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("author", comment.getAuthor());
            commentMap.put("content", comment.getContent());
            commentMap.put("timestamp", comment.getTimestamp());
            return commentMap;
        }).collect(Collectors.toList());

        response.put("comments", comments);
        return ResponseEntity.ok(response);
    }

    /** 新增留言 */
    @PostMapping("/api/projects/{projectId}/issues/{issueId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @RequestBody Comment comment) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));

        comment.setIssue(issue);
        comment.setTimestamp(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        return ResponseEntity.ok(savedComment);
    }




}

