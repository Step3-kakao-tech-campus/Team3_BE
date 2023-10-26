package com.bungaebowling.server.city.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles(value = {"test", "private", "aws"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CityControllerTest {

    private final MockMvc mvc;

    private final ObjectMapper om;


    @Autowired
    public CityControllerTest(MockMvc mvc, ObjectMapper om) {
        this.mvc = mvc;
        this.om = om;
    }

    @Test
    @DisplayName("시/도 조회 테스트")
    void getCities() throws Exception {
    }

    @Test
    @DisplayName("시/군/구 조회 테스트")
    void getCountries() throws Exception {
    }

    @Test
    @DisplayName("읍/면/동 조회 테스트")
    void getDistricts() throws Exception {
    }

    @Test
    @DisplayName("행정구역 정보 조회 테스트")
    void getDistrictInfo() throws Exception {
    }
}