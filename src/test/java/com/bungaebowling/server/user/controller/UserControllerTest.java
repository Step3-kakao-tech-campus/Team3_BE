package com.bungaebowling.server.user.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test", "private", "aws"})
@Sql(value = "classpath:test_db/teardown.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private RedisKeyValueAdapter redisKeyValueAdapter;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 테스트 - 성공")
    void join() throws Exception {
        // given
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto("testCode회원", "testCode@test.com", "testCode12!@", "1");

        String requestBody = om.writeValueAsString(joinDto);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .post("/api/join")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @DisplayName("로그인 테스트 - 성공")
    void login() throws Exception {
        //given
        UserRequest.LoginDto loginDto = new UserRequest.LoginDto("test@test.com", "test12!@");

        String requestBody = om.writeValueAsString(loginDto);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .post("/api/login")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("로그아웃 테스트 - 성공")
    void logout() throws Exception {
        // given
        BDDMockito.given(redisTemplate.delete(Mockito.anyString())).willReturn(null);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .post("/api/logout")
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("토큰 재발급 테스트 - 성공")
    void reIssueTokens() throws Exception {
        //given
        var userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var refreshToken = JwtProvider.createRefresh(userDetails.user());

        var refreshCookie = new Cookie("refreshToken", refreshToken);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .post("/api/authentication")
                        .cookie(refreshCookie)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    void sendVerification() {
    }

    @Test
    void confirmEmail() {
    }

    @Test
    void getUsers() {
    }

    @Test
    void getUser() {
    }

    @Test
    void getMyProfile() {
    }

    @Test
    void updateMyProfile() {
    }

    @Test
    void getUserRecords() {
    }
}