package com.bungaebowling.server.applicant.repository;

import com.bungaebowling.server.applicant.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query("select a from Applicant a join fetch a.user u join fetch a.post p where u.id = :userId and p.id = :postId")
    List<Applicant> findApplicantByPostId(Long userId, Long postId);

    @Query("select count(a) from Applicant a join fetch a.post p where p.id = :postId and a.status = true")
    int getApplicantNumber(Long postId);
}