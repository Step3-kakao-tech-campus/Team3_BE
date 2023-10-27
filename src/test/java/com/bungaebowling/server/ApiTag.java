package com.bungaebowling.server;

public enum ApiTag {
    AUTHORIZATION("회원 가입/로그인/인증"),
    CITY("행정 구역"),
    POST("모집글"),
    APPLICANT("신청"),
    COMMENT("댓글"),
    USER("개인 프로필/정보"),
    RECORD("참여 기록"),
    SCORE("볼링 점수(스코어)"),
    MESSAGE("쪽지");

    private final String tagName;

    public String getTagName() {
        return tagName;
    }

    ApiTag(String tagName) {
        this.tagName = tagName;
    }
}
