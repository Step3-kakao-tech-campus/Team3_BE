package com.bungaebowling.server.post.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PostControllerTest extends ControllerTestConfig {

    @Autowired
    public PostControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("게시글 조회")
    void getPosts() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        int size = 20;
        int key = 30;
        Long cityId = 1L;
        Long countryId = 1L;
        Long districtId = 1L;
        Boolean all = true;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .param("key", Integer.toString(key))
                        .param("size", Integer.toString(size))
                        .param("cityId", Long.toString(cityId))
                        .param("countryId", Long.toString(countryId))
                        .param("districtId", Long.toString(districtId))
                        .param("all", Boolean.toString(all))

        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.nextCursorRequest.key").isNumber(),
                jsonPath("$.response.nextCursorRequest.size").isNumber(),
                jsonPath("$.response.posts[0].id").isNumber(),
                jsonPath("$.response.posts[0].title").exists(),
                jsonPath("$.response.posts[0].dueTime").exists(),
                jsonPath("$.response.posts[0].districtName").exists(),
                jsonPath("$.response.posts[0].startTime").exists(),
                jsonPath("$.response.posts[0].userName").exists(),
                jsonPath("$.response.posts[0].currentNumber").isNumber(),
                jsonPath("$.response.posts[0].isClose").isBoolean()
        );
    }

    @Test
    void getPost() {
    }

    @Test
    void getUserParticipationRecords() {
    }

    @Test
    void createPost() {
    }

    @Test
    void updatePost() {
    }

    @Test
    void deletePost() {
    }

    @Test
    void patchPost() {
    }
}