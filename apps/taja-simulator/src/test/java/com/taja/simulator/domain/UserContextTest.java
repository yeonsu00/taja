package com.taja.simulator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserContext")
class UserContextTest {

    @Nested
    @DisplayName("계정 자동 생성")
    class AccountGeneration {

        @Test
        @DisplayName("이름은 'sim_'으로 시작한다")
        void name_startsWith_sim() {
            UserContext context = new UserContext("출퇴근러", "매일 따릉이로 출근");
            assertThat(context.getName()).startsWith("sim_");
        }

        @Test
        @DisplayName("이메일은 '@test.com'으로 끝난다")
        void email_endsWith_testCom() {
            UserContext context = new UserContext("출퇴근러", "매일 따릉이로 출근");
            assertThat(context.getEmail()).endsWith("@test.com");
        }

        @Test
        @DisplayName("두 UserContext는 서로 다른 이름과 이메일을 가진다")
        void two_contexts_have_different_accounts() {
            UserContext context1 = new UserContext("출퇴근러", "설명1");
            UserContext context2 = new UserContext("관광객", "설명2");

            assertThat(context1.getName()).isNotEqualTo(context2.getName());
            assertThat(context1.getEmail()).isNotEqualTo(context2.getEmail());
        }

        @Test
        @DisplayName("페르소나 이름과 설명이 그대로 저장된다")
        void persona_stored_correctly() {
            UserContext context = new UserContext("출퇴근러", "매일 따릉이로 출근");

            assertThat(context.getPersonaName()).isEqualTo("출퇴근러");
            assertThat(context.getPersonaDescription()).isEqualTo("매일 따릉이로 출근");
        }
    }

    @Nested
    @DisplayName("로그인 상태")
    class LoginStatus {

        @Test
        @DisplayName("초기 상태에서 isLoggedIn()은 false다")
        void initial_isLoggedIn_false() {
            UserContext context = new UserContext("출퇴근러", "설명");
            assertThat(context.isLoggedIn()).isFalse();
        }

        @Test
        @DisplayName("accessToken 설정 후 isLoggedIn()은 true다")
        void after_setAccessToken_isLoggedIn_true() {
            UserContext context = new UserContext("출퇴근러", "설명");
            context.setAccessToken("eyJhbGciOiJIUzI1NiJ9.token");

            assertThat(context.isLoggedIn()).isTrue();
        }
    }
}
