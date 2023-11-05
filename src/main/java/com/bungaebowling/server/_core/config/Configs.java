package com.bungaebowling.server._core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Configs {

    @Getter
    private static String domain;

    @Value("${bungaebowling.domain}")
    private void setDomain(String value) {
        domain = value;
    }


    public static final List<String> CORS = Collections.unmodifiableList(
            List.of("http://localhost:3000", // 리액트 개발용 3000포트
                    "http://127.0.0.1:3000",
                    "https://bungae.jagaldol.dev",
                    "https://k2b218aaad192a.user-app.krampoline.com")
    );

    public static List<String> getFullCORS() {
        List<String> fullCORS = new ArrayList<>(CORS);
        fullCORS.add(domain);
        return fullCORS;
    }
}
