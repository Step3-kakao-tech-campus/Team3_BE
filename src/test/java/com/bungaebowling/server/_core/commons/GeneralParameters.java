package com.bungaebowling.server._core.commons;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.SimpleType;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;

public enum GeneralParameters {
    CURSOR_KEY(parameterWithName("key").optional().type(SimpleType.NUMBER)
            .description("""
                    검색 기준 id
                                                                    
                    처음 요청 시 key 없이 요청 | 2번째 요청부터는 response.nextCursorRequest.key 값으로 요청
                                                                    
                    더이상 가져올 값이 없을 시 nextCursorRequest.key로 -1 응답
                    """)),
    SIZE(parameterWithName("size").optional().type(SimpleType.NUMBER).defaultValue(20).description("한번에 가져올 크기"));


    private final ParameterDescriptorWithType parameterDescriptorWithType;

    public ParameterDescriptorWithType getParameterDescriptorWithType() {
        return parameterDescriptorWithType;
    }

    GeneralParameters(ParameterDescriptorWithType responseDescriptor) {
        this.parameterDescriptorWithType = responseDescriptor;
    }
}
