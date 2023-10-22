package com.bungaebowling.server.user.repository;

import com.bungaebowling.server.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    List<User> findAllByNameContainingOrderByIdDesc(@Param("name") String name, Pageable pageable);

    List<User> findAllByNameContainingAndIdLessThanOrderByIdDesc(@Param("name") String name, @Param("key") Long key, Pageable pageable);
}
