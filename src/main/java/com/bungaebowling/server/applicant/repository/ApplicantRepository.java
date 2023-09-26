package com.bungaebowling.server.applicant.repository;

import com.bungaebowling.server.applicant.Applicant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    List<Applicant> findAllByPostIdOrderByIdAsc(Long userId, Long postId);

    List<Applicant> findAllByPostIdOrderByIdAsc(Pageable pageable, Long userId, Long postId);

    List<Applicant> findAllByPostIdLessThanOrderByIdAsc(Pageable pageable, Long key, Long userId, Long postId);

    @Query("select count(a) from Applicant a join a.post p where p.id = :postId and a.status = true")
    int getApplicantNumber(Long postId);

    @Modifying
    @Query("update Applicant a SET a.status = :status where a.id = :applicantId")
    void updateStatus(Long applicantId, Boolean status);
}