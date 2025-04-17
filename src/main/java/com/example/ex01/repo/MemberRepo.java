package com.example.ex01.repo;

import com.example.ex01.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepo extends JpaRepository<MemberEntity,Long> {

    MemberEntity findByUsername(String username);
}
