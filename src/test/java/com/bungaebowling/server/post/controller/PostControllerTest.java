package com.bungaebowling.server.post.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.commons.GeneralParameters;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.post.dto.PostRequest;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
    @DisplayName("모집글 목록 조회")
    void getPosts() throws Exception {
        //given
        int size = 20;
        int key = 30;
        Long cityId = 1L;
        Long countryId = 1L;
        Long districtId = 1L;
        Boolean all = Boolean.TRUE;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts")
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
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[post] getPosts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집글 목록 조회")
                                        .description("""
                                                모집글 목록를 조회합니다.
                                                """)
                                        .tag(ApiTag.POST.getTagName())
                                        .queryParameters(
                                                GeneralParameters.CURSOR_KEY.getParameterDescriptorWithType(),
                                                GeneralParameters.SIZE.getParameterDescriptorWithType(),
                                                parameterWithName("cityId").optional().type(SimpleType.NUMBER).description("시/도 ID (넘겨주지 않을 시 설정 시/도 설정 안 한 것)"),
                                                parameterWithName("countryId").optional().type(SimpleType.NUMBER).description("시/군/구 ID (넘겨주지 않을 시 설정 시/군/구 설정 안 한 것)"),
                                                parameterWithName("districtId").optional().type(SimpleType.NUMBER).description("읍/면/동 ID (넘겨주지 않을 시 설정 읍/면/동 설정 안 한 것)"),
                                                parameterWithName("all").type(SimpleType.BOOLEAN).defaultValue(true).description("전체 보기/모집 중 선택")

                                        )
                                        .responseSchema(Schema.schema("모집글 목록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.posts[].id").description("조회된 모집글 ID"),
                                                        fieldWithPath("response.posts[].title").description("조회된 모집글 제목 "),
                                                        fieldWithPath("response.posts[].dueTime").description("조회된 모집글 모집 마감기한"),
                                                        fieldWithPath("response.posts[].districtName").description("조회된 모집글 행정구역"),
                                                        fieldWithPath("response.posts[].startTime").description("조회된 모집글 게임 예정 일시"),
                                                        fieldWithPath("response.posts[].userName").description("조회된 모집글 작성자 이름 "),
                                                        fieldWithPath("response.posts[].profileImage").description("조회된 모집글 작성자 프로필 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.posts[].currentNumber").description("조회된 모집글 참석 인원"),
                                                        fieldWithPath("response.posts[].isClose").description("모집글 마감 여부")

                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집글 상세 조회")
    void getPost() throws Exception {
        // given
        Long postId = 1L;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}", postId)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.post.id").isNumber(),
                jsonPath("$.response.post.title").exists(),
                jsonPath("$.response.post.userId").isNumber(),
                jsonPath("$.response.post.userName").exists(),
                jsonPath("$.response.post.districtName").exists(),
                jsonPath("$.response.post.currentNumber").isNumber(),
                jsonPath("$.response.post.content").exists(),
                jsonPath("$.response.post.startTime").exists(),
                jsonPath("$.response.post.dueTime").exists(),
                jsonPath("$.response.post.viewCount").isNumber(),
                jsonPath("$.response.post.createdAt").exists(),
                jsonPath("$.response.post.editedAt").exists(),
                jsonPath("$.response.post.isClose").isBoolean()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[post] getPost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집글 상세 조회")
                                        .description("""
                                                모집글의 상세 정보를 조회합니다.
                                                """)
                                        .tag(ApiTag.POST.getTagName())
                                        .pathParameters(
                                                parameterWithName("postId").description("조회할 모집글 ID")
                                        )
                                        .responseSchema(Schema.schema("모집글 상세 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.post.id").description("모집글 ID"),
                                                        fieldWithPath("response.post.title").description("모집글 제목"),
                                                        fieldWithPath("response.post.userId").description("모집글 작성자 ID"),
                                                        fieldWithPath("response.post.userName").description("모집글 작성자 이름"),
                                                        fieldWithPath("response.post.profileImage").description("조회된 모집글 작성자 프로필 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.post.districtName").description("모집글 행정 구역"),
                                                        fieldWithPath("response.post.currentNumber").description("현재 모집 확정 인원 수"),
                                                        fieldWithPath("response.post.content").description("모집글 내용"),
                                                        fieldWithPath("response.post.startTime").description("게임 예정 일시"),
                                                        fieldWithPath("response.post.dueTime").description("모집 마감기한"),
                                                        fieldWithPath("response.post.viewCount").description("조회 수"),
                                                        fieldWithPath("response.post.createdAt").description("모집글 생성 시간"),
                                                        fieldWithPath("response.post.editedAt").description("모집글 수정 시간 "),
                                                        fieldWithPath("response.post.isClose").description("모집글 마감 여부")
                                                ))
                                        .build()
                        )
                )
        );

    }

    @Test
    @DisplayName("참여 기록 조회")
    void getUserParticipationRecords() throws Exception {
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
        String condition = "all";
        String status = "all";
        DateTime start = DateTime.now().minusMonths(3);
        DateTime end = DateTime.now();
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/users/{userId}/participation-records", userId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .param("key", Integer.toString(key))
                        .param("size", Integer.toString(size))
                        .param("condition", condition)
                        .param("status", status)
                        .param("cityId", Long.toString(cityId))
                        .param("start", start.toString("yyyy-MM-dd"))
                        .param("end", end.toString("yyyy-MM-dd"))
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
                jsonPath("$.response.posts[0].applicantId").isNumber(),
                jsonPath("$.response.posts[0].title").exists(),
                jsonPath("$.response.posts[0].dueTime").exists(),
                jsonPath("$.response.posts[0].districtName").exists(),
                jsonPath("$.response.posts[0].startTime").exists(),
                jsonPath("$.response.posts[0].currentNumber").isNumber(),
                jsonPath("$.response.posts[0].isClose").isBoolean(),
                jsonPath("$.response.posts[0].scores").exists(),
                jsonPath("$.response.posts[0].members[0].id").exists()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[post] getUserParticipationRecords",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("참여 기록 조회")
                                        .description("""
                                                참여 기록을 조회합니다.
                                                """)
                                        .tag(ApiTag.POST.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("userId").description("조회할 유저의 ID")
                                        )
                                        .queryParameters(
                                                GeneralParameters.CURSOR_KEY.getParameterDescriptorWithType(),
                                                GeneralParameters.SIZE.getParameterDescriptorWithType(),
                                                parameterWithName("condition").optional().type(SimpleType.STRING).defaultValue("all").description("모집글 유형 (종류 : 전체 보기 - all, 작성한 글 - created, 참여한 글 - participated)"),
                                                parameterWithName("status").optional().type(SimpleType.STRING).defaultValue("all").description("모집글 상태 (종류 : 전체 보기 - all, 모집중 - open, 모집 완료 - closed)"),
                                                parameterWithName("cityId").optional().type(SimpleType.NUMBER).description("모집 장소 (시/군/구)"),
                                                parameterWithName("start").optional().type(SimpleType.STRING).defaultValue(start.toString("yyyy-MM-dd")).description("조회 시작일자, 기본 값: 3개월 전"),
                                                parameterWithName("end").optional().type(SimpleType.STRING).defaultValue(end.toString("yyyy-MM-dd")).description("조회 종료일자, 기본 값: 현재 날짜")
                                        )
                                        .responseSchema(Schema.schema("참여 기록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.posts[].id").description("모집글 ID"),
                                                        fieldWithPath("response.posts[].applicantId").description("조회하고자 하는 유저의 ID가 모집글에 신청한 ID"),
                                                        fieldWithPath("response.posts[].title").description("모집글 제목 "),
                                                        fieldWithPath("response.posts[].dueTime").description("게임 마감 일시 "),
                                                        fieldWithPath("response.posts[].districtName").description("지역"),
                                                        fieldWithPath("response.posts[].startTime").description("게임 예정 일시 "),
                                                        fieldWithPath("response.posts[].currentNumber").description("현재 모집 확정 인원 수"),
                                                        fieldWithPath("response.posts[].isClose").description("모집글 마감 여부"),
                                                        fieldWithPath("response.posts[].scores").description("스코어 정보"),
                                                        fieldWithPath("response.posts[].scores[].id").optional().type(SimpleType.NUMBER).description("스코어 ID"),
                                                        fieldWithPath("response.posts[].scores[].score").optional().type(SimpleType.NUMBER).description("등록된 사용자 스코어"),
                                                        fieldWithPath("response.posts[].scores[].scoreImage").optional().type(SimpleType.STRING).description("등록된 사용자 스코어 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.posts[].members").description("모집 멤버 정보"),
                                                        fieldWithPath("response.posts[].members[].id").optional().type(SimpleType.NUMBER).description("모집 멤버 ID"),
                                                        fieldWithPath("response.posts[].members[].name").optional().type(SimpleType.STRING).description("모집 멤버 이름"),
                                                        fieldWithPath("response.posts[].members[].profileImage").optional().type(SimpleType.STRING).description("모집 멤버 프로필 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.posts[].members[].isRated").optional().type(SimpleType.BOOLEAN).description("모집 멤버 별점 입력 여부")
                                                ))
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집글 등록")
    void createPost() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        PostRequest.CreatePostDto requestDto = new PostRequest.CreatePostDto("테스트", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), "테스트", 1L);
        String requestBody = om.writeValueAsString(requestDto);
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)

        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.id").isNumber()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[post] createPost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집글 등록")
                                        .description("""
                                                모집글을 등록합니다.
                                                """)
                                        .tag(ApiTag.POST.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .requestSchema(Schema.schema("모집글 등록 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("title").description("모집글 제목 "),
                                                fieldWithPath("districtId").description("모집글 행정구역 ID"),
                                                fieldWithPath("startTime").description("게임 예정 일시"),
                                                fieldWithPath("dueTime").description("모집 마감기한"),
                                                fieldWithPath("content").description("모집글 내용")
                                        )
                                        .responseSchema(Schema.schema("모집글 등록 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.id").description("모집글 ID")
                                                ))
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집글 수정")
    void updatePost() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        Long postId = 1L;
        PostRequest.UpdatePostDto requestDto = new PostRequest.UpdatePostDto("테스트", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), "테스트");
        String requestBody = om.writeValueAsString(requestDto);
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .put("/api/posts/{postId}", postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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

    @Test
    void deletePost() {
    }

    @Test
    void patchPost() {
    }
}