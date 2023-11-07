package com.bungaebowling.server.user.rate.repository;

import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.user.rate.UserRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRateRepository extends JpaRepository<UserRate, Long> {

    List<UserRate> findAllByUserId(Long userId);

    @Query("SELECT ur FROM UserRate ur JOIN FETCH ur.user u WHERE ur.applicant.id = :applicantId")
    List<UserRate> findAllByApplicantId(@Param("applicantId") Long applicantId);

    void deleteAllByApplicant(Applicant applicant);

}
