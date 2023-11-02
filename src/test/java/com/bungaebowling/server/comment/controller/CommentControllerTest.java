package com.bungaebowling.server.comment.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // given
        Long postId = 1L;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/comments", postId)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));


        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.nextCursorRequest").exists(),
                jsonPath("$.response.comments[*].id", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].userId").hasJsonPath(),
                jsonPath("$.response.comments[*].userName").hasJsonPath(),
                jsonPath("$.response.comments[*].profileImage").hasJsonPath(),
                jsonPath("$.response.comments[*].content", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].createdAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].editedAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].id", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].childComments[*].userId", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].childComments[*].userName", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].profileImage").hasJsonPath(),
                jsonPath("$.response.comments[*].childComments[*].content", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].createdAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].editedAt", everyItem(notNullValue()))
        );
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