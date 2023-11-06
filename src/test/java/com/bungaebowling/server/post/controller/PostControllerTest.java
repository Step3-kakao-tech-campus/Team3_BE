package com.bungaebowling.server.post.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.commons.GeneralParameters;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
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
        Boolean all = Boolean.TRUE;

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
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
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