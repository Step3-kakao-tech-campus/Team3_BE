package com.bungaebowling.server.city.controller;

import com.bungaebowling.server.ApiTag;
import com.bungaebowling.server.ControllerTestConfig;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
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
@ActiveProfiles(value = {"test", "private", "aws"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CityControllerTest extends ControllerTestConfig {

    @Autowired
    public CityControllerTest(WebApplicationContext context, ObjectMapper om) {
        super(context, om);
    }

    @Test
    @DisplayName("시/도 조회 테스트")
    void getCities() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/cities")
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.cities[0].id").isNumber(),
                jsonPath("$.response.cities[0].name").exists()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "getCities",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("시/도 조회")
                                        .description("시/도 정보를 조회합니다.")
                                        .tag(ApiTag.CITY.getTagName())
                                        .requestFields()
                                        .responseSchema(Schema.schema("시-도 조회 DTO"))
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태 정보"),
                                                fieldWithPath("response").description("응답 body"),
                                                fieldWithPath("response.cities").description("시/도(city)정보 list"),
                                                fieldWithPath("response.cities[].id").description("시/도(city)의 ID"),
                                                fieldWithPath("response.cities[].name").description("시/도(city)의 이름"),
                                                fieldWithPath("errorMessage").description("에러 메시지")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("시/군/구 조회 테스트")
    void getCountries() throws Exception {
        // given
        Long cityId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/cities/{cityId}/countries", cityId)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.countries[0].id").isNumber(),
                jsonPath("$.response.countries[0].name").exists()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "getCountries",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("시/군/구 조회")
                                        .description("시/군/구 정보를 조회합니다.")
                                        .tag(ApiTag.CITY.getTagName())
                                        .pathParameters(parameterWithName("cityId").description("시/도 id"))
                                        .requestFields()
                                        .responseSchema(Schema.schema("시-군-구 조회 DTO"))
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태 정보"),
                                                fieldWithPath("response").description("응답 body"),
                                                fieldWithPath("response.countries").description("시/군/구(country)정보 list"),
                                                fieldWithPath("response.countries[].id").description("시/군/구(country)의 ID"),
                                                fieldWithPath("response.countries[].name").description("시/군/구(country)의 이름"),
                                                fieldWithPath("errorMessage").description("에러 메시지")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("읍/면/동 조회 테스트")
    void getDistricts() throws Exception {
        // given
        Long countryId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/cities/countries/{countryId}/districts", countryId)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.districts[0].id").isNumber(),
                jsonPath("$.response.districts[0].name").exists()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "getDistricts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("읍/면/동 조회")
                                        .description("읍/면/동 정보를 조회합니다.")
                                        .tag(ApiTag.CITY.getTagName())
                                        .pathParameters(parameterWithName("countryId").description("시/군/구 id"))
                                        .requestFields()
                                        .responseSchema(Schema.schema("읍-면-동 조회 DTO"))
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태 정보"),
                                                fieldWithPath("response").description("응답 body"),
                                                fieldWithPath("response.districts").description("읍/면/동(district)정보 list"),
                                                fieldWithPath("response.districts[].id").description("읍/면/동(district)의 ID"),
                                                fieldWithPath("response.districts[].name").description("읍/면/동(district)의 이름"),
                                                fieldWithPath("errorMessage").description("에러 메시지")
                                        )
                                        .build()
                        )
                )
        );
    }

    @Test
    @DisplayName("행정 구역 정보 조회 테스트")
    void getDistrictInfo() throws Exception {
        // given
        Long districtId = 1L;
        // when
        ResultActions resultActions = mvc.perform(
                RestDocumentationRequestBuilders
                        .get("/api/cities/districts/{districtId}", districtId)
        );
        // then
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Object json = om.readValue(responseBody, Object.class);
        System.out.println("[response]\n" + om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        resultActions.andExpectAll(
                status().isOk(),
                jsonPath("$.status").value(200),
                jsonPath("$.response.cityId").isNumber(),
                jsonPath("$.response.cityName").exists(),
                jsonPath("$.response.countryId").isNumber(),
                jsonPath("$.response.countryName").exists(),
                jsonPath("$.response.name").exists()
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "getDistrictInfo",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .summary("행정 구역 정보 조회")
                                        .description("행정 구역의 정보를 조회합니다.")
                                        .tag(ApiTag.CITY.getTagName())
                                        .pathParameters(parameterWithName("districtId").description("읍/면/동 id"))
                                        .requestFields()
                                        .responseSchema(Schema.schema("행정 구역 정보 조회 DTO"))
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태 정보"),
                                                fieldWithPath("response").description("응답 body"),
                                                fieldWithPath("response.cityId").description("시/도(city)의 ID"),
                                                fieldWithPath("response.cityName").description("시/도(city)의 이름"),
                                                fieldWithPath("response.countryId").description("시/군/구(country)의 ID"),
                                                fieldWithPath("response.countryName").description("시/군/구(country)의 이름"),
                                                fieldWithPath("response.name").description("읍/면/동(district)의 이름"),
                                                fieldWithPath("errorMessage").description("에러 메시지")
                                        )
                                        .build()
                        )
                )
        );
    }
}