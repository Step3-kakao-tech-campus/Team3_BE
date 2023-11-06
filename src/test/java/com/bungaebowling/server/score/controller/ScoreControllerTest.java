package com.bungaebowling.server.score.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ScoreControllerTest extends ControllerTestConfig {

    @Autowired
    public ScoreControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("점수 조회 테스트")
    void getScores() throws Exception {
        // given
        Long postId = 1L;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/scores", postId)
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
                        "[score] getScores",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("점수 조회")
                                        .description("""
                                                모집글의 점수들을 조회합니다.
                                                """
                                        )
                                        .tag(ApiTag.SCORE.getTagName())
                                        .pathParameters(parameterWithName("postId").description("모집글 id"))
                                        .responseSchema(Schema.schema("점수 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.SUCCESS.getResponseDescriptor().and(
                                                        fieldWithPath("response.scores").description("점수 목록"),
                                                        fieldWithPath("response.scores[].id").description("점수의 ID(PK)"),
                                                        fieldWithPath("response.scores[].scoreNum").description("볼링 점수"),
                                                        fieldWithPath("response.scores[].scoreImage").optional().type(SimpleType.STRING).description("점수 첨부 이미지 경로")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("점수 등록 테스트")
    void createScore() throws Exception {
    }

    @Test
    @DisplayName("점수 수정 테스트")
    void updateScore() throws Exception {
    }

    @Test
    @DisplayName("점수 이미지 삭제 테스트")
    void deleteScoreImage() throws Exception {
    }

    @Test
    @DisplayName("점수 삭제 테스트")
    void deleteScore() throws Exception {
    }
}