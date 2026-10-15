package com.MuniBardo.technique.repository;

import com.MuniBardo.technique.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    @Query("SELECT c FROM Citizen c WHERE c.cinOrPassport = :identifier")
    Optional<Citizen> findByCinOrPassport(@Param("identifier") String identifier);
}