package com.bungaebowling.server.applicant.controller;

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
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ApplicantControllerTest extends ControllerTestConfig {

    @Autowired
    public ApplicantControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("신청자 목록 조회 테스트")
    void getApplicants() throws Exception {
        // given
        Long postId = 1L;
        int size = 2;
        int key = 30;

        var userId = 1L; // 김볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/applicants", postId)
                        .param("key", Integer.toString(key))
                        .param("size", Integer.toString(size))
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.nextCursorRequest").exists(),
                jsonPath("$.response.applicantNumber").isNumber(),
                jsonPath("$.response.applicants[0].id").isNumber(),
                jsonPath("$.response.applicants[0].user.id").isNumber(),
                jsonPath("$.response.applicants[0].user.name").exists(),
                jsonPath("$.response.applicants[0].user.profileImage").hasJsonPath(),
                jsonPath("$.response.applicants[0].user.rating").isNumber(),
                jsonPath("$.response.applicants[0].status").isBoolean(),
                jsonPath("$.response.applicants[0].id").value(lessThan(key)),
                jsonPath("$.response.applicants").value(hasSize(lessThanOrEqualTo(size)))
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[applicant] getApplicants",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("신청자 목록 조회")
                                        .description("""
                                                모집글의 신청자 목록을 조회합니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(parameterWithName("postId").description("조회할 모집글 id"))
                                        .queryParameters(
                                                GeneralParameters.CURSOR_KEY.getParameterDescriptorWithType(),
                                                GeneralParameters.SIZE.getParameterDescriptorWithType()
                                        )
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema("신청자 목록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.applicantNumber").description("총 신청자의 수"),
                                                        fieldWithPath("response.applicants[].id").description("신청의 ID(PK)"),
                                                        fieldWithPath("response.applicants[].user.id").description("신청자의 ID(PK)"),
                                                        fieldWithPath("response.applicants[].user.name").description("신청자의 닉네임"),
                                                        fieldWithPath("response.applicants[].user.profileImage").optional().type(SimpleType.STRING).description("신청자의 프로필 이미지 경로"),
                                                        fieldWithPath("response.applicants[].user.rating").description("신청자의 별점"),
                                                        fieldWithPath("response.applicants[].status").description("신청 수락 여부 | true: 수락, false: 수락 대기")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집글에 대한 신청 테스트")
    void create() throws Exception {
    }

    @Test
    @DisplayName("모집자의 신청 수락 테스트")
    void accept() throws Exception {
    }

    @Test
    @DisplayName("모집자의 신청 거절 테스트")
    void reject() throws Exception {
    }

    @Test
    @DisplayName("자신의 신청 상태 조회 테스트")
    void checkStatus() throws Exception {
    }

    @Test
    @DisplayName("참여자에게 별점 등록 테스트")
    void rateUser() throws Exception {
    }
}