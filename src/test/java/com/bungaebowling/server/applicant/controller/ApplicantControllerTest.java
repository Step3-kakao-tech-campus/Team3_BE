package com.bungaebowling.server.applicant.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ApplicantControllerTest extends ControllerTestConfig {

    @Autowired
    public ApplicantControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("신청자 목록 조회 테스트")
    void getApplicants() throws Exception {
    }

    @Test
    @DisplayName("모집글에 대한 신청 테스트")
    void create() throws Exception {
    }

    @Test
    @DisplayName("모집자의 신청 수락 테스트")
    void accept() throws Exception {
    }

    @Test
    @DisplayName("모집자의 신청 거절 테스트")
    void reject() throws Exception {
    }

    @Test
    @DisplayName("자신의 신청 상태 조회 테스트")
    void checkStatus() throws Exception {
    }

    @Test
    @DisplayName("참여자에게 별점 등록 테스트")
    void rateUser() throws Exception {
    }
}