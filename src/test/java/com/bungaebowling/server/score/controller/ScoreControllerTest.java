package com.bungaebowling.server.score.controller;

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
class ScoreControllerTest extends ControllerTestConfig {

    @Autowired
    public ScoreControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("점수 조회 테스트")
    void getScores() throws Exception {
    }

    @Test
    @DisplayName("점수 등록 테스트")
    void createScore() throws Exception {
    }

    @Test
    @DisplayName("점수 수정 테스트")
    void updateScore() throws Exception {
    }

    @Test
    @DisplayName("점수 이미지 삭제 테스트")
    void deleteScoreImage() throws Exception {
    }

    @Test
    @DisplayName("점수 삭제 테스트")
    void deleteScore() throws Exception {
    }
}