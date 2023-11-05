package com.bungaebowling.server.message.controller;

import com.bungaebowling.server.ControllerTestConfig;
import com.bungaebowling.server._core.commons.ApiTag;
import com.bungaebowling.server._core.commons.GeneralApiResponseSchema;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.message.dto.MessageRequest;
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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MessageControllerTest extends ControllerTestConfig {

    @Autowired
    public MessageControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }


    @Test
    @DisplayName("대화방(쪽지) 목록 조회 테스트")
    void getOpponents() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/messages/opponents")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.messages[0].opponentUserId").isNumber(),
                jsonPath("$.response.messages[0].opponentUserName").exists(),
                jsonPath("$.response.messages[0].recentMessage").exists(),
                jsonPath("$.response.messages[0].recentTime").exists(),
                jsonPath("$.response.messages[0].countNew").isNumber()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[message] getOpponents",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("대화방(쪽지) 목록 조회")
                                        .description("""
                                                대화 목록를 조회합니다.                                     
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .responseSchema(Schema.schema("대화방 목록 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.messages[].opponentUserId").optional().description("쪽지 상대 유저 ID"),
                                                        fieldWithPath("response.messages[].opponentUserName").optional().type(SimpleType.STRING).description("쪽지 상대 유저 이름"),
                                                        fieldWithPath("response.messages[].opponentUserProfileImage").description("상대 프로필 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.messages[].recentMessage").description("쪽지 상대와의 가장 최근 메시지 내용"),
                                                        fieldWithPath("response.messages[].recentTime").description("쪽지 상대와의 가장 최근 송수신 시각"),
                                                        fieldWithPath("response.messages[].countNew").description("안 앍은 메시지의 수")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회 테스트")
    void getMessagesAndUpdateToRead() throws Exception {
        // given
        Long userId = 1L;
        Long opponentUserId = 3L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/messages/opponents/{opponentUserId}", opponentUserId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
        );
        // then
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.opponentUserName").exists(),
                jsonPath("$.response.messages[0].id").exists(),
                jsonPath("$.response.messages[0].content").exists(),
                jsonPath("$.response.messages[0].time").exists(),
                jsonPath("$.response.messages[0].isRead").isBoolean(),
                jsonPath("$.response.messages[0].isReceive").isBoolean()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "[message] getMessagesAndUpdateToRead",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("일대일 대화방 쪽지 조회")
                                        .description("""
                                                상대 유저와의 쪽지 목록을 조회합니다.                       
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .responseSchema(Schema.schema("일대일 대화방 쪽지 조회 응답 DTO"))
                                        .responseFields(
                                                GeneralApiResponseSchema.NEXT_CURSOR.getResponseDescriptor().and(
                                                        fieldWithPath("response.opponentUserName").description("쪽지 상대 유저 이름"),
                                                        fieldWithPath("response.opponentUserProfileImage").description("상대 프로필 사진 경로 | 사진이 없을 경우 null"),
                                                        fieldWithPath("response.messages[].id").description("쪽지 ID"),
                                                        fieldWithPath("response.messages[].content").description("쪽지 내용"),
                                                        fieldWithPath("response.messages[].time").description("쪽지 송신시간"),
                                                        fieldWithPath("response.messages[].isRead").description("쪽지대상이  읽었는지"),
                                                        fieldWithPath("response.messages[].isReceive").description("내가 받은 쪽지인지")
                                                )
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("쪽지 보내기 테스트")
    void sendMessage() throws Exception {
        // given
        Long userId = 1L;
        Long opponentUserId = 3L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링
        MessageRequest.SendMessageDto requestDto = new MessageRequest.SendMessageDto("쪽지보내기 테스트");
        String requestBody = om.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .post("/api/messages/opponents/{opponentId}", opponentUserId)
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
                        "[message] sendMessage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("쪽지 보내기")
                                        .description("""
                                                쪽지를 보냅니다.
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .requestSchema(Schema.schema("쪽지 보내기 요청 DTO"))
                                        .requestFields(fieldWithPath("content").description("송신할 쪽지 내용"))
                                        .pathParameters(
                                                parameterWithName("opponentId").description("쪽지를 수신할 유저의 Id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("쪽지함 삭제 테스트")
    void deleteMessagesByOpponentId() throws Exception {
        // given
        Long userId = 1L;
        Long opponentId = 3L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .delete("/api/messages/opponents/{opponentId}", opponentId)
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
                        "[message] deleteMessagesByOpponentId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("쪽지함 삭제")
                                        .description("""
                                                해당유저와의 모든 쪽지를 삭제합니다.(쪽지함을 삭제합니다.)
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("opponentId").description("쪽지를 삭제할 대화 상대의 Id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("쪽지 개별 삭제")
    void deleteMessageById() throws Exception {
        // given
        Long userId = 1L;
        Long messageId = 1L;
        String accessToken = JwtProvider.createAccess(
                User.builder()
                        .id(userId)
                        .role(Role.ROLE_USER)
                        .build()
        ); // 김볼링

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .delete("/api/messages/{messageId}", messageId)
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
                        "[message] deleteMessageById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("쪽지 개별 삭제")
                                        .description("""
                                                쪽지를 삭제합니다.
                                                """)
                                        .tag(ApiTag.MESSAGE.getTagName())
                                        .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("access token"))
                                        .pathParameters(
                                                parameterWithName("messageId").description("삭제할 쪽지 id")
                                        )
                                        .responseSchema(Schema.schema(GeneralApiResponseSchema.SUCCESS.getName()))
                                        .responseFields(GeneralApiResponseSchema.SUCCESS.getResponseDescriptor())
                                        .build()
                        )
                )
        );
    }

}
