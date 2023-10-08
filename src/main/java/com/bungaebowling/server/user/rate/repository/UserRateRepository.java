package com.bungaebowling.server.user.rate.repository;

import com.bungaebowling.server.user.rate.UserRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRateRepository extends JpaRepository<UserRate, Long> {

    List<UserRate> findAllByUserId(Long userId);
}
