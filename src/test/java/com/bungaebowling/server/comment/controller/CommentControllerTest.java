package com.bungaebowling.server.comment.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.commons.GeneralParameters;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.comment.Comment;
import com.bungaebowling.server.comment.dto.CommentRequest;
import com.bungaebowling.server.comment.repository.CommentRepository;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
class CommentControllerTest extends ControllerTestConfig {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentControllerTest(WebApplicationContext context, ObjectMapper om, CommentRepository commentRepository) {
        super(context, om);
        this.commentRepository = commentRepository;
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
                                                                                                
                                                기본적으로 **삭제된 댓글은 보이지 않**습니다.
                                                                                                
                                                삭제된 댓글이 대댓글을 가지고 있는 경우만 출력이 되며 이때 댓글은 아래와 같이 응답되게 됩니다.
                                                - userId, userName -> null
                                                - content -> "%s"
                                                                                                
                                                size에는 삭제된 댓글의 수가 함께 집계되기 때문에 응답에는 없지만 개수로 포함될 수 있습니다. 따라서 **설정한 size 수(default 20)보다 적은 수의 댓글이 응답될 수** 있습니다.
                                                """.formatted(Comment.DELETED_COMMENT_CONTENT))
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
                                                        fieldWithPath("response.comments[].content").description("댓글 내용 | 삭제된 댓글의 경우 '%s'".formatted(Comment.DELETED_COMMENT_CONTENT)),
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
                                                GeneralParameters.CURSOR_KEY.getParameterDescriptorWithType(),
                                                GeneralParameters.SIZE.getParameterDescriptorWithType()
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    void create() throws Exception {
        // given
        Long postId = 1L;

        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        var requestDto = new CommentRequest.CreateDto("안녕하세요");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts/{postId}/comments", postId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[comment] create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("댓글 등록")
                                        .description("""
                                                모집글에 새로운 댓글을 등록합니다.
                                                                                                
                                                대댓글 등록은 별개의 api를 사용해주세요.
                                                """)
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(parameterWithName("postId").description("댓글을 등록할 모집글 id"))
                                        .requestSchema(Schema.schema("댓글 등록 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("등록할 댓글 내용"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.CREATED.getName()))
                                        .responseFields(GeneralApiResponseSchema.CREATED.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("대댓글 등록 테스트")
    void createReply() throws Exception {
        // given
        var postId = 1L;

        var parentCommentId = 3L;

        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        var requestDto = new CommentRequest.CreateDto("확인 해볼게요!");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts/{postId}/comments/{commentId}/reply", postId, parentCommentId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
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
                        "[comment] createReply",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("대댓글 등록")
                                        .description("""
                                                댓글에 대댓글을 등록합니다.
                                                """)
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("대댓글을 등록할 모집글 id"),
                                                parameterWithName("commentId").description("대댓글을 등록할 댓글 id")
                                        )
                                        .requestSchema(Schema.schema("댓글 등록 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("등록할 댓글 내용"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.CREATED.getName()))
                                        .responseFields(GeneralApiResponseSchema.CREATED.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("대댓글 등록 테스트 - 삭제된 댓글에 시도")
    void createReplyAtDeleted() throws Exception {
        // given
        var postId = 1L;

        var parentCommentId = 1L;

        var userId = 1L;

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        var requestDto = new CommentRequest.CreateDto("확인 해볼게요!");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/posts/{postId}/comments/{commentId}/reply", postId, parentCommentId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));


        resultActions.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.response").value(ErrorCode.COMMENT_NOT_FOUND.toString())
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] createReplyAtDeleted",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("대댓글을 등록할 모집글 id"),
                                                parameterWithName("commentId").description("대댓글을 등록할 댓글 id")
                                        )
                                        .requestSchema(Schema.schema("댓글 등록 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("등록할 댓글 내용"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.FAIL.getName()))
                                        .responseFields(GeneralApiResponseSchema.FAIL.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void edit() throws Exception {
        // given
        var postId = 1L;

        var commentId = 3L;

        var userId = 3L; // 이볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        var content = "아 그냥 취소할게요";

        var requestDto = new CommentRequest.EditDto(content);
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        var modifiedComment = commentRepository.findById(commentId).orElse(null);

        Assertions.assertNotNull(modifiedComment);
        Assertions.assertEquals(modifiedComment.getUser().getId(), userId);
        Assertions.assertEquals(modifiedComment.getContent(), content);

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] edit",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("댓글 수정")
                                        .description("""
                                                댓글을 수정합니다.(대댓글의 경우도 commentId를 사용하여 해당 api를 사용합니다.)
                                                """)
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("댓글을 수정할 모집글 id"),
                                                parameterWithName("commentId").description("수정할 댓글 id")
                                        )
                                        .requestSchema(Schema.schema("댓글 수정 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("수정할 댓글 내용"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 수정 테스트 - 자신의 댓글이 아님")
    void editNotMyComment() throws Exception {
        // given
        var postId = 1L;

        var commentId = 2L;

        var userId = 3L; // 이볼링

        var accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        );

        var requestDto = new CommentRequest.EditDto("아 그냥 취소할게요");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));


        resultActions.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.response").value(ErrorCode.COMMENT_UPDATE_PERMISSION_DENIED.toString())
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] editNotMyComment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("댓글을 수정할 모집글 id"),
                                                parameterWithName("commentId").description("수정할 댓글 id")
                                        )
                                        .requestSchema(Schema.schema("댓글 수정 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("수정할 댓글 내용"))
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.FAIL.getName()))
                                        .responseFields(GeneralApiResponseSchema.FAIL.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void delete() throws Exception {
        // given
        var postId = 1L;

        var commentId = 2L;

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
                        .delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        var modifiedComment = commentRepository.findById(commentId).orElse(null);

        Assertions.assertNotNull(modifiedComment);
        Assertions.assertNull(modifiedComment.getUser());
        Assertions.assertEquals(modifiedComment.getContent(), Comment.DELETED_COMMENT_CONTENT);


        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200)
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("댓글 삭제")
                                        .description("""
                                                댓글을 삭제합니다.(대댓글의 경우도 commentId를 사용하여 해당 api를 사용합니다.)
                                                                                                
                                                삭제된 댓글이 대댓글을 가지고 있던 경우, 댓글 조회 api 호출 시 내용과 작성자가 지워진 채로 조회가 됩니다.
                                                - userId, userName -> null
                                                - content -> "%s"
                                                """)
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("댓글을 삭제할 모집글 id"),
                                                parameterWithName("commentId").description("삭제할 댓글 id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("댓글 삭제 테스트 - 자신의 댓글이 아님")
    void deleteNotMyComment() throws Exception {
        // given
        var postId = 1L;

        var commentId = 3L;

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
                        .delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));


        resultActions.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.response").value(ErrorCode.COMMENT_DELETE_PERMISSION_DENIED.toString())
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[comment] editNotMyComment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(ApiTag.COMMENT.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("postId").description("댓글을 삭제할 모집글 id"),
                                                parameterWithName("commentId").description("삭제할 댓글 id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.FAIL.getName()))
                                        .responseFields(GeneralApiResponseSchema.FAIL.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }
}