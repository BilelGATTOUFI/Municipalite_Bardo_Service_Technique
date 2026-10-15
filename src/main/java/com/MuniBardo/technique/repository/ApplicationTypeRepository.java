package com.MuniBardo.technique.repository;

import com.MuniBardo.technique.entity.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Integer> {
}