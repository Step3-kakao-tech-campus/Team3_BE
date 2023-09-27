package com.bungaebowling.server.applicant.repository;

import com.bungaebowling.server.applicant.Applicant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query("SELECT a FROM Applicant a JOIN a.user u JOIN a.post p WHERE u.id = :userId AND p.id = :postId")
    Optional<Applicant> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT a FROM Applicant a JOIN a.user u JOIN a.post p WHERE u.id = :userId AND p.id = :postId AND a.status = false ORDER BY a.id DESC")
    List<Applicant> findAllByUserIdAndPostIdOrderByIdDesc(@Param("userId") Long userId, @Param("postId") Long postId, Pageable pageable);

    @Query("SELECT a FROM Applicant a JOIN a.user u JOIN a.post p WHERE u.id = :userId AND p.id = :postId AND a.id < :key AND a.status = false ORDER BY a.id DESC")
    List<Applicant> findAllByUserIdAndPostIdLessThanOrderByIdDesc(@Param("key") Long key, @Param("userId") Long userId, @Param("postId") Long postId, Pageable pageable);

    @Query("SELECT count(a) FROM Applicant a JOIN a.post p WHERE p.id = :postId AND a.status = true")
    int getApplicantNumber(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Applicant a SET a.status = :status WHERE a.id = :applicantId")
    void updateStatus(@Param("applicantId") Long applicantId, @Param("status") Boolean status);
}