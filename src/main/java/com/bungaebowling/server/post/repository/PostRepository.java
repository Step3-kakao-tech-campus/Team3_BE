package com.bungaebowling.server.post.repository;

import com.bungaebowling.server.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p where p.country.id =:id")
    List<Post> findAllById(@Param("id") Integer id);

    // 모집글 마감 여부도 추가해서 조회
    @Query("SELECT p FROM Post p where p.country.id =:id and p.isClose = false")
    List<Post> findAllByIdWithCloseFalse(@Param("id") Integer id);

    @Query("SELECT p FROM Post p where p.isClose = false")
    List<Post> findAllWithCloseFalse();

}
