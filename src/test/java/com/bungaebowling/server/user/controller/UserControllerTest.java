package com.bungaebowling.server.user.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.commons.GeneralParameters;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.repository.UserRepository;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.context.WebApplicationContext;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.hamcrest.Matchers.*;
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

    @MockBean
    JavaMailSender javaMailSender;

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
                jsonPath("$.status").value(200),
                jsonPath("$.response.id").isNumber()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] join",
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
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.CREATED.getName()))
                                        .responseFields(GeneralApiResponseSchema.CREATED.getResponseDescriptor())
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
                        "[user] login",
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
    @DisplayName("로그인 테스트 - 없는 Email 혹은 틀린 PW")
    void loginFail() throws Exception {
        //given
        UserRequest.LoginDto loginDto = new UserRequest.LoginDto("test@test.com", "test12");

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
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.response").value(ErrorCode.LOGIN_FAILED.toString())
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] login - fail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestSchema(Schema.schema("로그인 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("email").type(SimpleType.STRING).description("로그인 이메일"),
                                                fieldWithPath("password").type(SimpleType.STRING).description("로그인 비밀번호")

                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.FAIL.getName()))
                                        .responseFields(GeneralApiResponseSchema.FAIL.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() throws Exception {
        // given
        BDDMockito.given(redisTemplate.delete(Mockito.anyString())).willReturn(null);

        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/logout")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[user] logout",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("로그아웃")
                                        .description("서버에 저장된 리프레시 토큰의 정보를 만료시킵니다.")
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reIssueTokens() throws Exception {
        //given

        var userId = 1L;

        var refreshToken = JwtProvider.createRefresh(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        var refreshCookie = new MockCookie("refreshToken", refreshToken);

        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);
        BDDMockito.given(redisTemplate.opsForValue().get(Mockito.any())).willReturn(refreshToken);
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] reIssueTokens",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("토큰 재발급")
                                        .description("""
                                                http-only cookie의 refresh 토큰을 사용하여 access token과 refresh 토큰을 재발급합니다.

                                                Request COOKIE: refreshToken={jwt_refresh_token}""")
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
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
    @DisplayName("인증 메일 발송 테스트")
    void sendVerification() throws Exception {
        // given

        var userId = 2L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 최볼링

        JavaMailSender javaMailSenderImpl = new JavaMailSenderImpl();

        BDDMockito.given(javaMailSender.createMimeMessage()).willReturn(javaMailSenderImpl.createMimeMessage());
        BDDMockito.willAnswer(invocation -> {
            return null;
        }).given(javaMailSender).send(Mockito.any(MimeMessagePreparator.class));

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/email-verification")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[user] sendVerification",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("인증 메일 발송")
                                        .description("""
                                                계정 정보의 email로 인증 메일을 보냅니다. email에는 url이 삽입되어 보내집니다.

                                                (e.g.) https://bungaebowling.com/email-verification?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicm9sZSI6IlJPTEVfVVNFUiIsInR5cGUiOiJlbWFpbC12ZXJpZmljYXRpb24iLCJleHAiOjE2OTU2MzU5MDh9.3AWusXvtgBiQN0GoegjKJw-fnaYSGVO1Ue0sSrtuWCVOQwzfIwh6KELN2NHOOXIO6MK-D11PndbtwcHetibZVQ
                                                                                                
                                                인증을 위해서 이 토큰 값을 그대로 /api/email-confirm의 데이터로 요청 보내주시길 바랍니다.
                                                """)
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

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
                RestDocumentationRequestBuilders
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] confirmEmail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("메일 인증")
                                        .description("""
                                                이메일로 받은 토큰을 사용하여 메일 인증을 수행합니다. 유저의 권한이 상승됩니다.
                                                """)
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestSchema(Schema.schema("메일 인증 요청 DTO"))
                                        .requestFields(fieldWithPath("token").type(SimpleType.STRING).description("이메일로 제공 받은 인증 토큰"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 테스트")
    void getUsers() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getUsers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("사용자 목록 조회")
                                        .description("""
                                                검색어를 포함하는 사용자를 모두 조회합니다.
                                                """)
                                        .tag(ApiTag.USER.getTagName())
                                        .responseSchema(Schema.schema("사용자 목록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.users[].id").description("사용자의 ID(PK)"),
                                                        fieldWithPath("response.users[].name").description("이름"),
                                                        fieldWithPath("response.users[].rating").description("사용자의 별점"),
                                                        fieldWithPath("response.users[].profileImage").type(SimpleType.STRING).optional().description("사용자의 프로필 이미지 링크 | 이미지 설정 안한 경우 null")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 테스트 - name 검색")
    void getUsersWithName() throws Exception {
        // given
        String searchName = "이볼";
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/users")
                        .param("name", searchName)
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpect(
                jsonPath("$.response.users[*].name", everyItem(containsString(searchName)))
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getUsersWithName",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.USER.getTagName())
                                        .queryParameters(parameterWithName("name").optional().description("검색할 유저의 이름(닉네임)"))
                                        .responseSchema(Schema.schema("사용자 목록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.users[].id").description("사용자의 ID(PK)"),
                                                        fieldWithPath("response.users[].name").description("이름(닉네임)"),
                                                        fieldWithPath("response.users[].rating").description("사용자의 별점"),
                                                        fieldWithPath("response.users[].profileImage").type(SimpleType.STRING).optional().description("사용자의 프로필 이미지 링크 | 이미지 설정 안한 경우 null")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 테스트 - 다음 페이지 검색")
    void getUsersNextPage() throws Exception {
        // given
        int key = 25;
        int size = 20;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/users")
                        .param("key", Integer.toString(key))
                        .param("size", Integer.toString(size))
        );

        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.users[0].id").value(lessThan(key)),
                jsonPath("$.response.users").value(hasSize(lessThanOrEqualTo(size)))
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getUsersNextPage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.USER.getTagName())
                                        .queryParameters(
                                                GeneralParameters.CURSOR_KEY.getParameterDescriptorWithType(),
                                                GeneralParameters.SIZE.getParameterDescriptorWithType()
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("사용자 상세 조회 테스트")
    void getUser() throws Exception {
        // given
        long userId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/users/{userId}", userId)
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("사용자 상세 조회")
                                        .description("""
                                                특정 사용자의 상세 정보를 확인합니다.
                                                 """)
                                        .tag(ApiTag.USER.getTagName())
                                        .pathParameters(
                                                parameterWithName("userId").type(SimpleType.NUMBER).description("사용자 id")
                                        )
                                        .responseSchema(Schema.schema("사용자 상세 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.name").description("사용자의 이름(닉네임)"),
                                                        fieldWithPath("response.averageScore").description("볼링 게임 평균 점수"),
                                                        fieldWithPath("response.rating").description("별점(매너 점수) | 별점 받은 적 없는 경우 0"),
                                                        fieldWithPath("response.address").description("사용자의 별점"),
                                                        fieldWithPath("response.profileImage").type(SimpleType.STRING).optional().description("사용자의 프로필 이미지 링크 | 이미지 설정 안한 경우 null")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("자신의 프로필 조회 테스트")
    void getMyProfile() throws Exception {
        // given
        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/users/mine")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getMyProfile",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("자신의 프로필 조회")
                                        .description("""
                                                자신의 프로필 정보를 조회합니다.
                                                 """)
                                        .tag(ApiTag.USER.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema("사용자 상세 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.id").description("사용자의 id(PK)"),
                                                        fieldWithPath("response.name").description("사용자의 이름(닉네임)"),
                                                        fieldWithPath("response.email").description("사용자의 이메일"),
                                                        fieldWithPath("response.verification").description("이메일 인증 여부"),
                                                        fieldWithPath("response.averageScore").description("볼링 게임 평균 점수"),
                                                        fieldWithPath("response.rating").description("별점(매너 점수) | 별점 받은 적 없는 경우 0"),
                                                        fieldWithPath("response.districtId").description("사용자가 설정한 기본 지역(행정구역 id)"),
                                                        fieldWithPath("response.address").description("설정한 기본 지역의 전체 명칭"),
                                                        fieldWithPath("response.profileImage").type(SimpleType.STRING).optional().description("사용자의 프로필 이미지 링크 | 이미지 설정 안한 경우 null")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("자신의 프로필 수정 테스트")
    void updateMyProfile() throws Exception {
        // given
        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        String newName = "김볼링싫어";
        long newDistrictId = 15L;
        MockMultipartFile file = new MockMultipartFile("profileImage", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        var builder = RestDocumentationRequestBuilders
                .multipart("/api/users/mine");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        ResultActions resultActions = mvc.perform(
                builder
                        .file(file)
                        .part(new MockPart("name", newName.getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("districtId", Long.toString(newDistrictId).getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] updateMyProfile",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("자신의 프로필 수정")
                                        .description("""
                                                자신의 프로필 정보를 수정합니다.
                                                                                                
                                                현재 사용 플러그인이 multipart/form-data의 파라미터에 대한 문서화가 지원되지 않습니다. (try it out 불가능)
                                                                                                
                                                아래 파라미터 중 변경 할 요소만 포함하여 보내면 됩니다.
                                                                                                
                                                | Part         | Type   | Description            |
                                                |--------------|--------|------------------------|
                                                | name         | String | 변경할 이름(닉네임)      |
                                                | districtId   | String | 변경할 기본 설정 지역 id |
                                                | profileImage | Binary | 변경할 프로필 이미지 파일 |
                                                                                                
                                                 """)
                                        .tag(ApiTag.USER.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("자신의 프로필 수정 테스트 - 이름만 변경")
    void updateMyProfileOnlyName() throws Exception {
        // given
        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        String newName = "김볼링싫어";
        // when
        var builder = RestDocumentationRequestBuilders
                .multipart("/api/users/mine");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        ResultActions resultActions = mvc.perform(
                builder
                        .part(new MockPart("name", newName.getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
    @DisplayName("자신의 프로필 수정 테스트 - 프로필 이미지만 변경")
    void updateMyProfileOnlyProfileImage() throws Exception {
        // given
        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        MockMultipartFile file = new MockMultipartFile("profileImage", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        var builder = RestDocumentationRequestBuilders
                .multipart("/api/users/mine");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        ResultActions resultActions = mvc.perform(
                builder
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                RestDocumentationRequestBuilders
                        .get("/api/users/{userId}/records", userId)
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] getUserRecords",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("사용자 기록(경기수, 평균점수) 조회")
                                        .description("""
                                                사용자의 경기 기록을 조회합니다.
                                                 """)
                                        .tag(ApiTag.USER.getTagName())
                                        .pathParameters(parameterWithName("userId").type(SimpleType.NUMBER).description("사용자 id"))
                                        .responseSchema(Schema.schema("사용자 기록(경기수, 평균점수) 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.name").description("사용자의 이름(닉네임)"),
                                                        fieldWithPath("response.game").description("참여 게임 수"),
                                                        fieldWithPath("response.average").description("볼링 게임 평균 점수(참여 기록 없을 시 0)"),
                                                        fieldWithPath("response.maximum").description("볼링 게임 최고 점수(참여 기록 없을 시 0)"),
                                                        fieldWithPath("response.minimum").description("볼링 게임 최저 점수(참여 기록 없을 시 0)")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("비밀번호 변경")
    void updatePassword() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        UserRequest.UpdatePasswordDto requestDto = new UserRequest.UpdatePasswordDto("test12!@", "qwer1234!");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .patch("/api/users/password")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[message] updatePassword",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("비밀번호 변경")
                                        .description("""
                                                비밀번호를 변경합니다.
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .requestSchema(Schema.schema("비밀번호 변경 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("password").description("기존 비밀번호"),
                                                fieldWithPath("newPassword").description("새로운 비밀번호")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("비밀번호 찾기 - 본인 인증 메일 발송")
    void sendVerificationMailForPasswordReset() throws Exception {
        // given
        UserRequest.SendVerificationMailForPasswordResetDto requestDto = new UserRequest.SendVerificationMailForPasswordResetDto("test@test.com");
        String requestBody = om.writeValueAsString(requestDto);

        JavaMailSender javaMailSenderImpl = new JavaMailSenderImpl();

        BDDMockito.given(javaMailSender.createMimeMessage()).willReturn(javaMailSenderImpl.createMimeMessage());
        BDDMockito.willAnswer(invocation -> {
            return null;
        }).given(javaMailSender).send(Mockito.any(MimeMessagePreparator.class));
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/password/email-verification")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[user] sendVerificationMailForPasswordReset",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("비밀번호 찾기 - 본인 인증 메일 발송")
                                        .description("""
                                                계정 정보의 email로 인증 메일을 보냅니다. email에는 url이 삽입되어 보내집니다.

                                                (e.g.) https://bungaebowling.com/password/email-verification?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicm9sZSI6IlJPTEVfVVNFUiIsInR5cGUiOiJlbWFpbC12ZXJpZmljYXRpb24iLCJleHAiOjE2OTU2MzU5MDh9.3AWusXvtgBiQN0GoegjKJw-fnaYSGVO1Ue0sSrtuWCVOQwzfIwh6KELN2NHOOXIO6MK-D11PndbtwcHetibZVQ
                                                                                                
                                                인증을 위해서 이 토큰 값을 그대로 /api/password/email-verification의 데이터로 요청 보내주시길 바랍니다.
                                                """)
                                        .tag(ApiTag.AUTHORIZATION.getTagName())
                                        .requestSchema(Schema.schema("비밀번호 찾기 - 본인 인증 메일 발송 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("email").description("가입한 이메일")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("비밀번호 찾기 - 본인 인증 메일 확인 및 임시 비밀번호 메일 발송")
    void confirmEmailAndSendTempPassword() throws Exception {
        // given
        User user = User.builder()
                .id(2L)
                .build();
        String token = JwtProvider.createEmailVerificationForPassword(user);
        UserRequest.ConfirmEmailAndSendTempPasswordDto requestDto = new UserRequest.ConfirmEmailAndSendTempPasswordDto(token);
        String requestBody = om.writeValueAsString(requestDto);

        JavaMailSender javaMailSenderImpl = new JavaMailSenderImpl();

        BDDMockito.given(javaMailSender.createMimeMessage()).willReturn(javaMailSenderImpl.createMimeMessage());
        BDDMockito.willAnswer(invocation -> {
            return null;
        }).given(javaMailSender).send(Mockito.any(MimeMessagePreparator.class));
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/password/email-confirm")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        );
    }


}