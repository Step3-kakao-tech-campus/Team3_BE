package com.bungaebowling.server.user.rate.repository;

import com.bungaebowling.server.user.rate.UserRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRateRepository extends JpaRepository<UserRate, Long> {
}
