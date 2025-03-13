package com.scsb.pm.dao;

import com.scsb.pm.entity.Issue;
import com.scsb.pm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
//    List<Issue> findByAssignee(String assignee);
    List<Issue> findByProject_Id(Long projectId);
    List<Issue> findByProjectAndDeletedFalse(Project project);
}
