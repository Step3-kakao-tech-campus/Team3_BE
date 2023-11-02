package com.bungaebowling.server.comment.controller;

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
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CommentControllerTest extends ControllerTestConfig {

    @Autowired
    public CommentControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("댓글 조회 테스트")
    void getComments() throws Exception {
        // given
        Long postId = 1L;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/comments", postId)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));


        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.nextCursorRequest").exists(),
                jsonPath("$.response.comments[*].id", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].userId").hasJsonPath(),
                jsonPath("$.response.comments[*].userName").hasJsonPath(),
                jsonPath("$.response.comments[*].profileImage").hasJsonPath(),
                jsonPath("$.response.comments[*].content", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].createdAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].editedAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].id", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].childComments[*].userId", everyItem(isA(Integer.class))),
                jsonPath("$.response.comments[*].childComments[*].userName", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].profileImage").hasJsonPath(),
                jsonPath("$.response.comments[*].childComments[*].content", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].createdAt", everyItem(notNullValue())),
                jsonPath("$.response.comments[*].childComments[*].editedAt", everyItem(notNullValue()))
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] getComments",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("댓글 조회")
                                        .description("""
                                                모집글의 댓글들을 조회합니다.
                                                """)
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .pathParameters(parameterWithName("postId").description("조회할 댓글들의 모집글 id"))
                                        .responseSchema(Schema.schema("댓글 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.comments[].id").description("댓글의 ID(PK)"),
                                                        fieldWithPath("response.comments").description("댓글"),
                                                        fieldWithPath("response.comments[].userId").optional().type(SimpleType.NUMBER).description("댓글 작성자의 ID(PK) | 삭제된 댓글의 경우 null"),
                                                        fieldWithPath("response.comments[].userName").optional().type(SimpleType.STRING).description("댓글 작성자의 닉네임 | 삭제된 댓글의 경우 null"),
                                                        fieldWithPath("response.comments[].profileImage").optional().type(SimpleType.STRING).description("댓글 작성자 프로필 이미지 경로"),
                                                        fieldWithPath("response.comments[].content").description("댓글 내용 | 삭제된 댓글의 경우 '삭제된 댓글입니다.'"),
                                                        fieldWithPath("response.comments[].createdAt").description("댓글 생성 시간"),
                                                        fieldWithPath("response.comments[].editedAt").description("댓글 마지막 수정 시간"),
                                                        fieldWithPath("response.comments[].childComments").description("대댓글"),
                                                        fieldWithPath("response.comments[].childComments[].id").description("대댓글 ID"),
                                                        fieldWithPath("response.comments[].childComments[].userId").description("대댓글 작성자의 ID(PK)"),
                                                        fieldWithPath("response.comments[].childComments[].userName").description("대댓글 작성자의 닉네임"),
                                                        fieldWithPath("response.comments[].childComments[].profileImage").description("대댓글 작성자 프로필 이미지 경로"),
                                                        fieldWithPath("response.comments[].childComments[].content").description("대댓글 내용"),
                                                        fieldWithPath("response.comments[].childComments[].createdAt").description("대댓글 생성 시간"),
                                                        fieldWithPath("response.comments[].childComments[].editedAt").description("대댓글 마지막 수정 시간")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 조회 테스트 - key, size 검색")
    void getCommentsWithPage() throws Exception {
        // given
        Long postId = 1L;
        int size = 2;
        int key = 1;

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/posts/{postId}/comments", postId)
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
                jsonPath("$.response.comments[0].id").value(greaterThan(key)),
                jsonPath("$.response.comments").value(hasSize(lessThanOrEqualTo(size)))
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] getCommentsWithPage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .pathParameters(parameterWithName("postId").description("조회할 댓글들의 모집글 id"))
                                        .queryParameters(
                                                parameterWithName("key").optional().type(SimpleType.NUMBER)
                                                        .description("""
                                                                검색 기준 id
                                                                                                                
                                                                처음 요청 시 key 없이 요청 | 2번째 요청부터는 response.nextCursorRequest.key 값으로 요청
                                                                                                                
                                                                더이상 가져올 값이 없을 시 nextCursorRequest.key로 -1 응답
                                                                """),
                                                parameterWithName("size").optional().type(SimpleType.NUMBER).defaultValue(20).description("한번에 가져올 크기")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    void create() throws Exception {
    }

    @Test
    @DisplayName("대댓글 등록 테스트")
    void createReply() throws Exception {
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void edit() throws Exception {
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void delete() throws Exception {
    }
}