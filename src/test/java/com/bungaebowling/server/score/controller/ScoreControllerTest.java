package com.bungaebowling.server.score.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
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
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ScoreControllerTest extends ControllerTestConfig {

    @MockBean
    private AmazonS3 amazonS3Client;

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
        // given
        Long postId = 1L;

        var userId = 1L; // 김볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        int score = 153;
        MockMultipartFile file = new MockMultipartFile("image", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        var builder = RestDocumentationRequestBuilders
                .multipart("/api/posts/{postId}/scores", postId);
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("POST");
                return request;
            }
        });
        ResultActions resultActions = mvc.perform(
                builder
                        .file(file)
                        .part(new MockPart("score", Integer.toString(score).getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
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
                        "[score] createScore",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("점수 등록")
                                        .description("""
                                                모집 완료되어 게임 플레이 한 이후(start_time 이후) 자신이 참여한 모집글에 점수 등록이 가능합니다.
                                                                                                
                                                - 파일은 png, jpg, gif, jpeg만 업로드 가능합니다.
                                                - 파일은 10MB의 크기 제한이 존재합니다.
                                                            
                                                현재 사용 플러그인이 multipart/form-data의 파라미터에 대한 문서화가 지원되지 않습니다. (try it out 불가능)
                                                                                                
                                                | Part  | Type   | Description                  |
                                                |-------|--------|------------------------------|
                                                | score | number | 볼링 점수 (1~300)             |
                                                | image | Binary | 점수판 사진 등 첨부 이미지 파일 |
                                                                                                
                                                 """)
                                        .tag(ApiTag.SCORE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(parameterWithName("postId").type(SimpleType.NUMBER).description("모집글 id"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("점수 수정 테스트")
    void updateScore() throws Exception {
        // given
        Long postId = 1L;

        Long scoreId = 2L;

        var userId = 1L; // 김볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        int score = 153;
        MockMultipartFile file = new MockMultipartFile("image", "image.png", MediaType.IMAGE_PNG_VALUE, "mockImageData".getBytes());

        String imageUrl = "https://kakao.com";

        BDDMockito.given(amazonS3Client.putObject(Mockito.any())).willReturn(null);
        BDDMockito.given(amazonS3Client.getUrl(Mockito.any(), Mockito.any())).willReturn(new URL(imageUrl));

        // when
        var builder = RestDocumentationRequestBuilders
                .multipart("/api/posts/{postId}/scores/{scoreId}", postId, scoreId);
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
                        .part(new MockPart("score", Integer.toString(score).getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
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
                        "[score] updateScore",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("점수 수정")
                                        .description("""
                                                자신의 점수에 대한 정보를 수정 가능합니다.
                                                                                                
                                                요청에 포함된 image나 score에 대하여 기존 정보를 수정합니다.
                                                                                                
                                                - 파일은 png, jpg, gif, jpeg만 업로드 가능합니다.
                                                - 파일은 10MB의 크기 제한이 존재합니다.
                                                            
                                                현재 사용 플러그인이 multipart/form-data의 파라미터에 대한 문서화가 지원되지 않습니다. (try it out 불가능)
                                                                                                
                                                아래 파라미터 중 변경 할 요소만 포함하여 보내면 됩니다.
                                                                                                
                                                | Part  | Type   | Description                  |
                                                |-------|--------|------------------------------|
                                                | score | number | 볼링 점수 (1~300)             |
                                                | image | Binary | 점수판 사진 등 첨부 이미지 파일 |
                                                                                                
                                                 """)
                                        .tag(ApiTag.SCORE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").type(SimpleType.NUMBER).description("모집글 id"),
                                                parameterWithName("scoreId").type(SimpleType.NUMBER).description("점수 id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
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