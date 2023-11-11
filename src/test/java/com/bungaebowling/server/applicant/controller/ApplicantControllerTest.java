package com.bungaebowling.server.applicant.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.commons.GeneralParameters;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.applicant.dto.ApplicantRequest;
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
import org.springframework.http.MediaType;
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
        // given
        Long postId = 2L;

        var userId = 4L; // 박볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts/{postId}/applicants", postId)
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
                        "[applicant] create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집글에 대한 신청")
                                        .description("""
                                                마감되지 않은 모집글에 신청을 합니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(parameterWithName("postId").description("신청할 모집글 id"))
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집자의 신청 수락 테스트")
    void accept() throws Exception {
        // given
        Long postId = 8L;

        Long applicantId = 18L;

        var userId = 3L; // 이볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        var requestDto = new ApplicantRequest.UpdateDto(true);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .put("/api/posts/{postId}/applicants/{applicantId}", postId, applicantId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[applicant] accept",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집자의 신청 수락")
                                        .description("""
                                                모집자가 신청을 승낙 할 수 있습니다.
                                                                                                
                                                status를 수정 가능합니다. true: 승낙 / false: 승낙 대기
                                                                                                
                                                거절은 DELETE 요청으로 신청을 완전히 삭제 해주시길 바랍니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(
                                                parameterWithName("postId").description("모집글 id"),
                                                parameterWithName("applicantId").description("수락할 신청의 Id")
                                        )
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .requestSchema(Schema.schema("모집자의 신청 수락 요청 DTO"))
                                        .requestFields(fieldWithPath("status").type(SimpleType.BOOLEAN).description("변경하고자 하는 신청의 상태 | true: 승낙, false: 승낙 대기"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("모집자의 신청 거절 테스트")
    void reject() throws Exception {
        // given
        Long postId = 8L;

        Long applicantId = 18L;

        var userId = 3L; // 이볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .delete("/api/posts/{postId}/applicants/{applicantId}", postId, applicantId)
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
                        "[applicant] reject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("모집자의 신청 거절")
                                        .description("""
                                                모집자가 신청을 거절 합니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(
                                                parameterWithName("postId").description("모집글 id"),
                                                parameterWithName("applicantId").description("거절할 신청의 Id")
                                        )
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("자신의 신청 상태 조회 테스트")
    void checkStatus() throws Exception {
        // given
        Long postId = 1L;

        var userId = 4L; // 박볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/applicants/check-status", postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.applicantId").isNumber(),
                jsonPath("$.response.isApplied").isBoolean(),
                jsonPath("$.response.isAccepted").isBoolean()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[applicant] checkStatus",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("자신의 신청 상태 조회")
                                        .description("""
                                                해당 모집글에 대한 자신의 신청 상태를 확인 합니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(parameterWithName("postId").description("조회할 모집글 id"))
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .responseSchema(Schema.schema("자신의 신청 상태 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.applicantId").optional().type(SimpleType.NUMBER).description("신청의 ID(PK)"),
                                                        fieldWithPath("response.isApplied").description("신청 상태(true: 신청됨 / false: 신청되지 않음"),
                                                        fieldWithPath("response.isAccepted").description("승낙 상태(true: 승낙됨 / false: 승낙되지 않음")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("참여자에게 별점 등록 테스트")
    void rateUser() throws Exception {
        // given
        Long postId = 3L;

        Long applicantId = 28L;

        var userId = 4L; // 박볼링

        var targetId = 1L;

        var rating = 4;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        var requestDto = new ApplicantRequest.RateDto(targetId, rating);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts/{postId}/applicants/{applicantId}/rating", postId, applicantId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[applicant] rateUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("참여자에게 별점 등록")
                                        .description("""
                                                같은 모집글에 참여한 사람들에게 별점을 등록할 수 있습니다.
                                                                                                
                                                별점은 모집완료(모집글의 is_close가 true) 후 게임 시작 시간(start_time)이 지나야 등록 가능합니다.
                                                """)
                                        .tag(ApiTag.APPLICANT.getTagName())
                                        .pathParameters(
                                                parameterWithName("postId").description("모집글 id"),
                                                parameterWithName("applicantId").description("자신의 신청 id")
                                        )
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .requestSchema(Schema.schema("참여자에게 별점 등록 요청 DTO"))
                                        .requestFields(
                                                fieldWithPath("targetId").type(SimpleType.NUMBER).description("평가 대상 사용자의 id(PK)"),
                                                fieldWithPath("rating").type(SimpleType.NUMBER).description("별점 | 1 ~ 5 범위 가능")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }
}