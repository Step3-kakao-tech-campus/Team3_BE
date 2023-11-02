package com.bungaebowling.server.comment.controller;

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
class CommentControllerTest extends ControllerTestConfig {

    @Autowired
    public CommentControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("댓글 조회 테스트")
    void getComments() throws Exception {
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    void create() throws Exception {
    }

    @Test
    @DisplayName("대댓글 등록 테스트")
    void createReply() throws Exception {
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void edit() throws Exception {
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void delete() throws Exception {
    }
}