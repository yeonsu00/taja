## 🚴‍♀️ 타자 (Taja)

**서울시 공공자전거 대여소별 이용 패턴을 시각화하고, 대여소별 채팅 기능을 제공하는 웹 서비스**

- 기본적인 대여소 정보만 제공하는 기존 따릉이 앱의 한계를 개선하고자, **대여소별 이용 통계**, **오늘의 남은 자전거 수 예측**, **대여소별 채팅**, **근처 대여소 안내** 등 다양한 기능 제공
- 서울시 공공자전거 데이터를 **시간대·요일·기온별로 분석**하여 이용 패턴 제공
- 이용자 간 **실시간 소통이 가능한 대여소별 채팅 기능** 지원

---
### 🧩 주요 기능
#### 1. 대여소 정보 및 통계
- 대여소별 실시간 자전거 수 조회
- 시간대별 / 요일별 / 기온별 이용 패턴 분석
- 집계 테이블 기반의 통계 조회로 성능 최적화
- Redis 기반 캐싱 및 실시간 데이터 수집 스케줄러

#### 2. 대여소별 채팅
- 채팅방은 대여소 단위로 구분

#### 3. 데이터 수집 및 예측
- 서울시 공공자전거 API 주기적 수집 (Spring Scheduler)
- 과거 데이터를 기반으로 오늘의 남은 자전거 수 예측

---
### ⚙️ 기술 스택

| 분야 | 사용 기술 |
|------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.4 |
| Build Tool | Gradle |
| Database | MySQL, Redis |
| Infra / DevOps | Docker, GitHub Actions (CI/CD) |
| Others | Querydsl, Swagger (Springdoc OpenAPI) |


---
### 🔗 링크
- Frontend Repository: 
- Backend Repository: [taja](https://github.com/yeonsu00/taja)
- 배포 URL: 
