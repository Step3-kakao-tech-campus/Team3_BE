package com.bungaebowling.server.user.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.bungaebowling.server.ApiTag;
import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server.GeneralApiResponseSchema;
import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.repository.UserRepository;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserControllerTest extends ControllerTestConfig {


    private final UserRepository userRepository;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private RedisKeyValueAdapter redisKeyValueAdapter;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private AmazonS3 amazonS3Client;

    @Autowired
    public UserControllerTest(WebApplicationContext context, ObjectMapper om, UserRepository userRepository) {
        super(context, om);
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("회원가입 테스트")
    void join() throws Exception {
        // given
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto("testCode회원", "testCode@test.com", "testCode12!@", "1");

        String requestBody = om.writeValueAsString(joinDto);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "join",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("회원가입")
                                        .description("""
                                                유저로 회원가입합니다.
                                                                                                
                                                회원가입 성공 시, 로그인과 똑같이 액세스 토큰과 리프레시 토큰을 전달합니다.

                                                - 가입 후 메일 인증 필요""")
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestSchema(Schema.schema("회원가입 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("name").type(SimpleType.STRING).description("닉네임 | 한글, 영문, 숫자만 가능/ 최대 20글자"),
                                                fieldWithPath("email").type(SimpleType.STRING).description("이메일 | 이메일(^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$) 최대 100자"),
                                                fieldWithPath("password").type(SimpleType.STRING).description("비밀번호 | 영문,숫자, 특수문자가 모두 포함 / 8자에서 64자 이내"),
                                                fieldWithPath("districtId").type(SimpleType.NUMBER).description("행정 구역 ID")
                                        )
                                        .responseHeaders(
                                                headerWithName(HttpHeaders.SET_COOKIE).description("""
                                                        refresh token(http-only cookie)

                                                        (e.g.)refreshToken={jwt_refresh_token}"""),
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("""
                                                        access token

                                                        (e.g.)Bearer {jwt_access_token}""")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("로그인 테스트")
    void login() throws Exception {
        //given
        UserRequest.LoginDto loginDto = new UserRequest.LoginDto("test@test.com", "test12!@");

        String requestBody = om.writeValueAsString(loginDto);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("로그인")
                                        .description("""
                                                 로그인 진행 시 액세스 토큰과 리프레시 토큰을 전달합니다.
                                                                                                 
                                                 액세스 토큰의 구조는 아래와 같습니다.
                                                                                                 
                                                 ```json
                                                {
                                                  "sub": "2",
                                                  "role": "ROLE_USER",
                                                 "type": "access",
                                                  "exp": 1695633312
                                                }
                                                 ```
                                                 
                                                 | 항목 | description                                               |
                                                 |------|-----------------------------------------------------------|
                                                 | sub  | 유저의 ID(PK)                                             |
                                                 | role | 권한(이메일 인증 시 ROLE_USER / 인증 안한 경우 ROLE_PENDING) |
                                                 | exp  | 유효기간                                                  |
                                                 | type | 토큰의 종류(access, refresh, email-verfication 등이 존재)   |
                                                  
                                                 """)
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestSchema(Schema.schema("로그인 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("email").type(SimpleType.STRING).description("로그인 이메일"),
                                                fieldWithPath("password").type(SimpleType.STRING).description("로그인 비밀번호")

                                        )
                                        .responseHeaders(
                                                headerWithName(HttpHeaders.SET_COOKIE).description("""
                                                        refresh token(http-only cookie)

                                                        (e.g.)refreshToken={jwt_refresh_token}"""),
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("""
                                                        access token

                                                        (e.g.)Bearer {jwt_access_token}""")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("로그아웃 테스트")
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
    @DisplayName("토큰 재발급 테스트")
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

//    @Test
//    @WithUserDetails(value = "최볼링")
//    @DisplayName("인증 메일 발송 테스트")
//    void sendVerification() throws Exception {
//        // when
//        ResultActions resultActions = mvc.perform(
//                MockMvcRequestBuilders
//                        .post("/api/email-verification")
//        );
//
//        // then
//        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
//        Object json = om.readValue(responseBody, Object.class);
//        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));
//
//        resultActions.andExpectAll(
//                status().isOk(),
//                jsonPath("$.status").value(200)
//        );
//    }

    @Test
    @DisplayName("메일 인증 테스트")
    void confirmEmail() throws Exception {
        // given
        User user = User.builder()
                .id(2L)
                .build();
        var token = JwtProvider.createEmailVerification(user);

        UserRequest.ConfirmEmailDto requestDto = new UserRequest.ConfirmEmailDto(token);

        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .post("/api/email-confirm")
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
    @DisplayName("사용자 목록 조회 테스트")
    void getUsers() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/users")
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.nextCursorRequest").exists(),
                jsonPath("$.response.users[0].id").exists(),
                jsonPath("$.response.users[0].name").exists(),
                jsonPath("$.response.users[0].rating").isNumber(),
                jsonPath("$.response.users[0].profileImage").hasJsonPath()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 테스트 - name 검색")
    void getUsersWithName() throws Exception {
        // given
        String searchName = "볼링";
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/users")
                        .param("name", searchName)
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpect(
                jsonPath("$.response.users[*].name", everyItem(containsString(searchName)))
        );
    }

    @Test
    @DisplayName("사용자 상세 조회 테스트")
    void getUser() throws Exception {
        // given
        long userId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/users/" + userId)
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.name").exists(),
                jsonPath("$.response.averageScore").isNumber(),
                jsonPath("$.response.rating").isNumber(),
                jsonPath("$.response.address").exists(),
                jsonPath("$.response.profileImage").hasJsonPath()
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("자신의 프로필 조회 테스트")
    void getMyProfile() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/users/mine")
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.id").exists(),
                jsonPath("$.response.name").exists(),
                jsonPath("$.response.email").exists(),
                jsonPath("$.response.verification").isBoolean(),
                jsonPath("$.response.averageScore").isNumber(),
                jsonPath("$.response.rating").isNumber(),
                jsonPath("$.response.districtId").isNumber(),
                jsonPath("$.response.address").exists(),
                jsonPath("$.response.profileImage").hasJsonPath()
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("자신의 프로필 수정 테스트")
    void updateMyProfile() throws Exception {
        // given
        String newName = "김볼링싫어";
        long newDistrictId = 15L;
        MockMultipartFile file = new MockMultipartFile("profileImage", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/users/mine")
                        .file(file)
                        .part(new MockPart("name", newName.getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("districtId", Long.toString(newDistrictId).getBytes(StandardCharsets.UTF_8)))
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        var user = userRepository.findByName(newName).orElse(null);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getName(), newName);
        Assertions.assertEquals(user.getDistrict().getId(), newDistrictId);
        Assertions.assertEquals(user.getImgUrl(), imageUrl);

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("자신의 프로필 수정 테스트 - 이름만 변경")
    void updateMyProfileOnlyName() throws Exception {
        // given
        String newName = "김볼링싫어";
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/users/mine")
                        .part(new MockPart("name", newName.getBytes(StandardCharsets.UTF_8)))
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        var user = userRepository.findByName(newName).orElse(null);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getName(), newName);

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @WithUserDetails(value = "김볼링")
    @DisplayName("자신의 프로필 수정 테스트 - 프로필 이미지만 변경")
    void updateMyProfileOnlyProfileImage() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("profileImage", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/users/mine")
                        .file(file)
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        var user = userRepository.findByName("김볼링").orElse(null);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getImgUrl(), imageUrl);

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }

    @Test
    @DisplayName("유저 기록 조회")
    void getUserRecords() throws Exception {
        // given
        Long userId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/users/" + userId + "/records")
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.name").exists(),
                jsonPath("$.response.game").isNumber(),
                jsonPath("$.response.average").isNumber(),
                jsonPath("$.response.maximum").isNumber(),
                jsonPath("$.response.minimum").isNumber()
        );
    }
}