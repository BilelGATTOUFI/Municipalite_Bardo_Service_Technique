package com.MuniBardo.technique.repository;

import com.MuniBardo.technique.entity.ApplicationStatus;
import com.MuniBardo.technique.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByFileNumber(String fileNumber);
    List<Application> findByCurrentStatus(ApplicationStatus status);
}