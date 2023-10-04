package com.bungaebowling.server.comment.repository;


import com.bungaebowling.server.comment.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c " +
            "FROM Comment c LEFT JOIN FETCH c.user u " +
            "WHERE c.post.id = :postId AND c.parent = null " +
            "ORDER BY c.id")
    List<Comment> findAllByPostIdAndIsParentNullOrderById(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c " +
            "FROM Comment c JOIN FETCH c.user u " +
            "WHERE c.parent.id = :parentId " +
            "ORDER BY c.id")
    List<Comment> findAllByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c " +
            "FROM Comment c LEFT JOIN FETCH c.user u " +
            "WHERE c.post.id = :postId AND c.parent = null AND c.id > :key " +
            "ORDER BY c.id")
    List<Comment> findAllByPostIdAndIsParentNullAndIdGreaterThanOrderById(@Param("postId") Long postId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.post.id = :postId AND c.parent = null")
    Optional<Comment> findByIdAndPostIdAndParentNull(@Param("id") Long id, @Param("postId") Long postId);
}
