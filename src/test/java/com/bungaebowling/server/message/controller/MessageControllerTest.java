package com.bungaebowling.server.message.controller;

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
public class MessageControllerTest extends ControllerTestConfig {

    @Autowired
    public MessageControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }


    @Test
    @DisplayName("대화방(쪽지) 목록 조회 테스트")
    void getOpponents() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/messages/opponents")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.messages[0].opponentUserId").isNumber(),
                jsonPath("$.response.messages[0].opponentUserName").exists(),
                jsonPath("$.response.messages[0].recentMessage").exists(),
                jsonPath("$.response.messages[0].recentTime").exists(),
                jsonPath("$.response.messages[0].countNew").isNumber()
        );
    }


}
