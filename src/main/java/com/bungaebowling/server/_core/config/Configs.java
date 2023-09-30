package com.bungaebowling.server._core.config;

import java.util.Collections;
import java.util.List;

public class Configs {
    public final static List<String> CORS = Collections.unmodifiableList(
            List.of("http://localhost:3000", // 리액트 개발용 3000포트
                    "http://127.0.0.1:3000")
    );
}
