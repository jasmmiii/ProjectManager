package com.scsb.pm.service;

import com.scsb.pm.dao.ProjectRepository;
import com.scsb.pm.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    public Project createProject(Project project){
        return projectRepository.save(project);
    }
}
