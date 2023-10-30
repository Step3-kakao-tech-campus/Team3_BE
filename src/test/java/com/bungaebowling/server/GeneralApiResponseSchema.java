package com.bungaebowling.server;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.epages.restdocs.apispec.SimpleType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public enum GeneralApiResponseSchema {
    SUCCESS(
            "성공 응답 DTO",
            new FieldDescriptors(
                    fieldWithPath("status").type(SimpleType.NUMBER).description("응답 상태 정보"),
                    fieldWithPath("response").type(SimpleType.STRING).optional().description("응답 body"),
                    fieldWithPath("errorMessage").type(SimpleType.STRING).optional().description("에러 메시지")
            )
    ),
    FAIL("실패 응답 DTO",
            new FieldDescriptors(
                    fieldWithPath("status").type(SimpleType.NUMBER).description("응답 상태 정보"),
                    fieldWithPath("response").type(SimpleType.STRING).description("에러 코드"),
                    fieldWithPath("errorMessage").type(SimpleType.STRING).description("에러 메시지")
            )
    ),
    NEXT_CURSOR("커서 페이징 응답 DTO",
            new FieldDescriptors(
                    fieldWithPath("status").type(SimpleType.NUMBER).description("응답 상태 정보"),
                    fieldWithPath("response").type(SimpleType.STRING).optional().description("응답 body"),
                    fieldWithPath("errorMessage").type(SimpleType.STRING).optional().description("에러 메시지"),
                    fieldWithPath("response.nextCursorRequest").description("다음 요청 cursor 정보(받은 값 그대로 다음 요청에 사용할 것)"),
                    fieldWithPath("response.nextCursorRequest.key").description("다음 요청 cursor의 key"),
                    fieldWithPath("response.nextCursorRequest.size").description("다음 요청 cursor의 size")
            )
    );

    private final String name;
    private final FieldDescriptors responseDescriptor;

    public FieldDescriptors getResponseDescriptor() {
        return responseDescriptor;
    }

    public String getName() {
        return name;
    }

    GeneralApiResponseSchema(String name, FieldDescriptors responseDescriptor) {
        this.name = name;
        this.responseDescriptor = responseDescriptor;
    }
}
