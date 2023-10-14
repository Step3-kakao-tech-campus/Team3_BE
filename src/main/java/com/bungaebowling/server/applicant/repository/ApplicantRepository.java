package com.bungaebowling.server.applicant.repository;

import com.bungaebowling.server.applicant.Applicant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query("SELECT a FROM Applicant a WHERE a.user.id = :userId AND a.post.id = :postId")
    Optional<Applicant> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.user u WHERE a.post.id = :postId AND u.id != :userId ORDER BY a.id DESC")
    List<Applicant> findAllByPostIdAndUserIdNotOrderByIdDesc(@Param("postId") Long postId, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.user u WHERE a.post.id = :postId AND u.id != :userId AND a.id < :key ORDER BY a.id DESC")
    List<Applicant> findAllByPostIdAndUserIdNotLessThanOrderByIdDesc(@Param("key") Long key, @Param("postId") Long postId, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT count(a) FROM Applicant a WHERE a.post.id = :postId AND a.status = true")
    Long countByPostIdAndIsStatusTrue(@Param("postId") Long postId);

    @Query("SELECT count(a) FROM Applicant a WHERE a.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.post p WHERE a.id = :id")
    Optional<Applicant> findByIdJoinFetchPost(@Param("id") Long id);

    List<Applicant> findAllByUserIdAndPostIsCloseTrueAndStatusTrue(Long userId);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.user u WHERE a.post.id = :postId and a.status = true  ORDER BY u.id DESC")
    List<Applicant> findAllByPostIdAndStatusTrueOrderByUserIdDesc(@Param("postId") Long postId);
}