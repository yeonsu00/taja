package com.taja.member.domain;

import lombok.Getter;

@Getter
public enum EmailForm {
    EMAIL_AUTH("TAJA 이메일 인증 번호",
            "<html><body><h1>TAJA 인증 코드: %s</h1><p>인증 코드 유효시간은 5분입니다.</p></body></html>");

    private final String title;
    private final String contentTemplate;

    EmailForm(String title, String contentTemplate) {
        this.title = title;
        this.contentTemplate = contentTemplate;
    }

    public String getContent(String authCode) {
        return String.format(contentTemplate, authCode);
    }
}
