package com.scsb.pm.dao;

import com.scsb.pm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE "
            + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (COALESCE(:status, '') = '' OR p.status = :status) "
            + "AND (COALESCE(:createdBy, '') = '' OR p.createdBy = :createdBy)")

    List<Project> searchProjects(@Param("name") String name,
                                @Param("status") String status,
                                @Param("createdBy") String createdBy);
}
