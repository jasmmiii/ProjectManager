package com.scsb.pm.dao;

import com.scsb.pm.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUsername(String username);

}
