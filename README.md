# Terraform SaaS Platform SSO Server Backend

> 프로젝트는 Terraform SaaS 플랫폼의 인증/SSO 서버 역할을 수행하는 Spring Boot 기반 백엔드입니다.

---

## 1. 프로젝트 개요

이 프로젝트는 `opentofu.click` 도메인 기반 Terraform SaaS 플랫폼에서 사용자 인증, 회원가입, 로그인 상태 확인, JWT 쿠키 발급/재발급, 사용자 승인 관리, 공개키 제공 기능을 담당하는 SSO 서버입니다.

주요 역할은 다음과 같습니다.

- 이메일/비밀번호 기반 로그인
- 회원가입 신청 및 승인 대기 상태 관리
- 이메일 인증 코드 발송
- JWT Access Token / Refresh Token 생성
- JWT 쿠키 기반 로그인 유지
- Access Token 만료 시 Refresh Token 기반 재발급
- JWT 검증용 공개키 제공
- 관리자용 가입 승인 목록 조회 및 승인
- `java-sso.opentofu.click` Host 제한 필터 적용

---

## 2. 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.1 |
| Web | Spring Web MVC |
| Security | Spring Security, BCrypt |
| Persistence | Spring Data JPA |
| Database | MySQL |
| Mail | Spring Boot Mail, Gmail SMTP |
| JWT | jjwt 0.11.2, RS256 |
| Build | Maven Wrapper |
| SSL | PKCS12 Keystore |
| Runtime Port | 443 |

---

## 3. 전체 아키텍처 요약

```text
Client
  |
  | HTTPS + Cookie(jwtAccessToken, jwtRefreshToken)
  v
SSO Server
  |
  +-- DomainFilter
  |     - Host: java-sso.opentofu.click 검증
  |
  +-- JwtAccessTokenFilter
  |     - Access Token 검증
  |     - 만료 시 request attribute에 상태 기록
  |
  +-- JwtRefreshTokenFilter
  |     - Refresh Token 검증
  |     - Access Token 재발급 가능 여부 제공
  |
  +-- Controller Layer
  |     - Login
  |     - LoginCheck
  |     - Logout
  |     - SignUp
  |     - EmailAuth
  |     - LoadApprovals
  |     - JwtPublicKeyProvider
  |
  +-- Service Layer
  |     - LoginService
  |     - LoginCheckService
  |     - SignUpService
  |     - LoadApprovalsService
  |
  +-- Repository Layer
        - UserRepository
        - MySQL users table
```

---

## 4. 패키지 구조

```text
click.opentofu.sso
├── SsoApplication.java
├── config
│   ├── FilterConfig.java
│   └── SecurityConfig.java
├── controller
│   ├── EmailAuth.java
│   ├── JwtPublicKeyProvider.java
│   ├── LoadApprovals.java
│   ├── Login.java
│   ├── LoginCheck.java
│   ├── Logout.java
│   └── SignUp.java
├── dto
│   └── User.java
├── entity
│   └── UserEntity.java
├── filter
│   ├── DomainFilter.java
│   ├── JwtAccessTokenFilter.java
│   └── JwtRefreshTokenFilter.java
├── repository
│   └── UserRepository.java
├── service
│   ├── LoadApprovalsService.java
│   ├── LoginCheckService.java
│   ├── LoginService.java
│   └── SignUpService.java
└── util
    └── JwtGenerator.java
```

---

## 5. 핵심 도메인 모델

### 5.1 User DTO

파일: `src/main/java/click/opentofu/sso/dto/User.java`

```java
public class User {
    private String uuid;
    private String password;
    private String email;
}
```

외부 요청 바디를 받는 DTO입니다. Jackson snake_case 전략이 적용되어 있습니다.

---

### 5.2 UserEntity

파일: `src/main/java/click/opentofu/sso/entity/UserEntity.java`

```java
@Entity
@Table(name = "users")
public class UserEntity {
    private String uuid;
    private String password;
    @Id private String emailId;
    private Boolean isEnabled;
    private Boolean isAdmin;
    private String dbTable;
    @Column(unique = true)
    private Integer userIndex;
}
```

`users` 테이블에 매핑되는 인증 사용자 엔티티입니다.

| 필드 | 의미 |
|---|---|
| uuid | 사용자 고유 UUID |
| emailId | 로그인 ID, JPA 기본키 |
| password | BCrypt 해시 비밀번호 |
| isEnabled | 승인 여부 |
| isAdmin | 관리자 여부 |
| dbTable | 사용자별 테이블 식별자 |
| userIndex | 사용자 순번, unique |

---

## 6. 주요 API

### 6.1 회원가입

```http
POST /api/v1/auth/sign-up
```

요청 예시:

```json
{
  "email": "user@example.com",
  "password": "plain-password"
}
```

동작:

1. 이메일 중복 여부 확인
2. 신규 사용자 저장
3. 비밀번호 BCrypt 해시 처리
4. `isEnabled=false`, `isAdmin=false` 상태로 생성
5. 관리자 또는 운영자에게 가입 신청 메일 발송

응답:

| 상태 | 의미 |
|---|---|
| 200 OK | 가입 신청 저장 성공 |
| 409 Conflict | 이미 존재하는 이메일 |

---

### 6.2 이메일 인증 코드 발송

```http
POST /api/v1/auth/email/send-validation-code
```

동작:

1. 6자리 숫자 인증 코드 생성
2. Gmail SMTP를 통해 사용자 이메일로 코드 발송
3. 생성된 인증 코드를 응답으로 반환

주의:

현재 코드는 인증 코드를 서버 저장소에 저장하거나 만료 검증하지 않고 응답으로 반환합니다. 운영 보안 관점에서는 서버 측 저장/검증 구조가 필요합니다.

---

### 6.3 로그인

```http
POST /api/v1/auth/login
```

요청 예시:

```json
{
  "email": "user@example.com",
  "password": "plain-password"
}
```

동작:

1. 이메일로 사용자 조회
2. BCrypt로 비밀번호 검증
3. 로그인 성공 시 JWT Access Token / Refresh Token 발급
4. 두 토큰을 HttpOnly Secure Cookie로 설정

쿠키 정책:

| 쿠키 | 만료 | 속성 |
|---|---:|---|
| jwtAccessToken | 10분 | HttpOnly, Secure, SameSite=None, Domain=opentofu.click |
| jwtRefreshToken | 6시간 | HttpOnly, Secure, SameSite=None, Domain=opentofu.click |

응답:

| 상태 | 의미 |
|---|---|
| 200 OK | 로그인 성공 |
| 401 Unauthorized | 이메일 또는 비밀번호 불일치 |

---

### 6.4 로그인 상태 확인 / 토큰 재발급

```http
POST /api/v1/auth/login-check
```

동작:

1. `JwtAccessTokenFilter`가 Access Token 검증
2. `JwtRefreshTokenFilter`가 Refresh Token 검증
3. Refresh Token이 유효하면 사용자 정보를 응답에 포함
4. Access Token이 만료되었고 Refresh Token이 유효하면 Access Token 재발급
5. Access Token과 Refresh Token의 subject가 다르면 401 반환

응답 주요 필드:

| 필드 | 의미 |
|---|---|
| userIndex | 사용자 순번 |
| email | 사용자 이메일 |
| isEmail | 이메일 존재 여부 |
| isAdmin | 관리자 여부 |
| isEnabled | 승인 여부 |
| dbTable | 사용자별 테이블명 |
| remainingTimeInMillis | Access Token 남은 시간 |
| refreshTokenRemainingTime | Refresh Token 남은 시간 |

---

### 6.5 로그아웃

```http
POST /api/v1/auth/logout
```

동작:

- `jwtAccessToken` 쿠키 삭제
- `jwtRefreshToken` 쿠키 삭제
- `remainingTimeInMillis=0` 반환

---

### 6.6 JWT 공개키 제공

```http
GET /api/v1/public-key
```

동작:

- 서버의 RSA 공개키를 Base64 인코딩 문자열로 반환
- 다른 서비스가 JWT 검증에 사용할 수 있는 구조

주의:

현재 RSA KeyPair는 애플리케이션 기동 시점마다 새로 생성됩니다. 서버 재시작 시 기존 토큰은 검증 불가능해질 수 있습니다.

---

### 6.7 가입 승인 정보 조회

```http
POST /api/v1/request/accounts/load-approvals
```

요청:

```json
{
  "email": "user@example.com"
}
```

응답:

```json
{
  "email": "user@example.com",
  "isEmail": true,
  "isAdmin": false,
  "isEnabled": false,
  "dbTable": "..."
}
```

---

### 6.8 전체 사용자 조회

```http
POST /api/v1/request/accounts/load-all
```

동작:

- `users` 테이블의 모든 사용자 승인 상태를 반환합니다.

주의:

현재 컨트롤러 레벨에서 관리자 권한 검사가 명시적으로 보이지 않습니다. 운영 환경에서는 관리자 권한 검증이 필요합니다.

---

### 6.9 사용자 승인

```http
POST /api/v1/request/accounts/approve
```

요청:

```json
{
  "email": "user@example.com"
}
```

동작:

- 해당 사용자의 `isEnabled` 값을 `true`로 변경합니다.

---

## 7. 인증/인가 흐름

### 7.1 로그인 성공 흐름

```text
1. Client -> POST /api/v1/auth/login
2. LoginController -> LoginService.validationLogin()
3. UserRepository.findById(email)
4. PasswordEncoder.matches(raw, encoded)
5. JwtGenerator.generateToken(email, access TTL)
6. JwtGenerator.generateToken(email, refresh TTL)
7. Set-Cookie(jwtAccessToken, jwtRefreshToken)
8. Client는 이후 요청에서 쿠키 자동 전송
```

---

### 7.2 로그인 체크 / Access Token 재발급 흐름

```text
1. Client -> POST /api/v1/auth/login-check
2. DomainFilter Host 검증
3. JwtAccessTokenFilter Access Token 검증
4. JwtRefreshTokenFilter Refresh Token 검증
5. LoginCheckController가 request attribute 확인
6. Refresh Token 유효 + Access Token 만료이면 Access Token 재발급
7. 사용자 상태 정보 반환
```

---

### 7.3 필터 체인 순서

파일: `src/main/java/click/opentofu/sso/config/FilterConfig.java`

| 순서 | 필터 | 역할 |
|---:|---|---|
| 1 | DomainFilter | Host 헤더 제한 |
| 2 | JwtAccessTokenFilter | Access Token 검증 |
| 3 | JwtRefreshTokenFilter | Refresh Token 검증 |

---

## 8. 보안 구성

### 8.1 Spring Security

파일: `src/main/java/click/opentofu/sso/config/SecurityConfig.java`

- BCryptPasswordEncoder Bean 등록
- 일부 API에 대해 CSRF ignore 설정
- CORS 기본 설정 활성화

CSRF 제외 경로:

```text
/api/v1/auth/login-check
/api/v1/auth/login
/api/v1/auth/sign-up
/api/v1/auth/logout
/api/v1/auth/email/send-validation-code
/api/v1/request/accounts/load-approvals
/api/v1/request/accounts/load-all
/api/v1/request/accounts/approve
/api/v1/public-key
```

---

### 8.2 Host 제한

파일: `src/main/java/click/opentofu/sso/filter/DomainFilter.java`

허용 Host:

```text
java-sso.opentofu.click
```

이외 Host 요청은 RuntimeException을 발생시킵니다.

---

### 8.3 JWT

파일: `src/main/java/click/opentofu/sso/util/JwtGenerator.java`

- RSA 2048 KeyPair 사용
- RS256 알고리즘 사용
- Claim에 `uuid`, `roles` 포함
- Subject는 사용자 이메일

현재 roles는 다음 고정 값입니다.

```json
["read", "write", "execute"]
```

---

## 9. 설정 파일

파일: `src/main/resources/application.properties`

주요 설정:

| 항목 | 값 |
|---|---|
| 서버 포트 | 443 |
| SSL KeyStore | classpath:keystore.p12 |
| Jackson Naming | SNAKE_CASE |
| Log File | /home/ec2-user/log/java-sso-app.log |
| DB | MySQL `mysql.private.mysql:3306/sso` |
| JPA DDL | update |
| Mail | Gmail SMTP |

---

## 10. 로컬 실행 방법

### 10.1 요구사항

- Java 21
- Maven Wrapper 사용 가능 환경
- MySQL 접근 가능 환경
- `src/main/resources/keystore.p12` 존재
- Gmail SMTP 설정 필요

---

### 10.2 빌드

```bash
./mvnw clean package
```

---

### 10.3 테스트

```bash
./mvnw test
```

---

### 10.4 실행

```bash
./mvnw spring-boot:run
```

또는:

```bash
java -jar target/sso-0.0.1-SNAPSHOT.jar
```

기본 포트는 `443`입니다. 일반 사용자 권한으로 443 포트를 바인딩할 수 없는 환경에서는 포트 변경 또는 권한 설정이 필요합니다.

---

## 11. 운영 배포 시 확인할 점

### 11.1 HTTPS / 인증서

현재 애플리케이션 내장 SSL 설정을 사용합니다.

```properties
server.port=443
server.ssl.key-store=classpath:keystore.p12
```

운영에서는 다음 중 하나를 선택할 수 있습니다.

- 애플리케이션 직접 TLS 종료
- Nginx/ALB/Ingress에서 TLS 종료 후 내부 HTTP 통신

---

### 11.2 쿠키 도메인

현재 JWT 쿠키는 다음 도메인으로 설정됩니다.

```text
opentofu.click
```

따라서 하위 도메인 간 SSO 공유가 가능합니다.

---

### 11.3 CORS Origin

주로 허용된 Origin:

```text
https://studio.opentofu.click
https://java.opentofu.click
```

컨트롤러마다 허용 Origin이 다릅니다. 실제 프론트엔드 도메인과 API 호출 도메인을 기준으로 통합 점검이 필요합니다.

---

## 12. 현재 코드 기준 주요 리스크 및 개선 필요 사항

아래 항목은 코드 분석 기준으로 확인된 운영 리스크입니다.

### 12.1 민감정보 하드코딩

`application.properties`에 다음 정보가 평문으로 포함되어 있습니다.

- SSL keystore password
- Gmail app password
- MySQL username/password

운영에서는 즉시 다음 방식으로 이전하는 것이 안전합니다.

- 환경 변수
- Kubernetes Secret
- AWS Secrets Manager
- SOPS/SealedSecrets
- Vault

---

### 12.2 JWT RSA 키가 서버 재시작마다 변경됨

`JwtGenerator`는 `@PostConstruct`에서 매번 RSA KeyPair를 새로 생성합니다.

문제:

- 서버 재시작 시 기존 JWT 검증 불가
- 다중 인스턴스 운영 시 인스턴스 간 토큰 검증 불일치 가능

개선:

- 외부 KeyStore에서 RSA key 로드
- Kubernetes Secret으로 private/public key 주입
- key id(kid) 기반 JWKS 구조 도입

---

### 12.3 Refresh Token 서버 저장소 없음

현재 Refresh Token은 쿠키에만 존재하며 서버 측 저장/폐기 목록이 없습니다.

문제:

- 강제 로그아웃/토큰 폐기 어려움
- 탈취된 Refresh Token 무효화 어려움

개선:

- Refresh Token DB 저장
- Token family / rotation
- logout 시 refresh token revoke
- reuse detection

---

### 12.4 이메일 인증 코드 검증 구조 미완성

현재 인증 코드는 생성 후 메일로 발송되고 응답에도 반환됩니다.

문제:

- 실제 검증 단계가 없음
- 응답으로 인증 코드가 노출됨
- 만료 시간 검증이 서버 측에 없음

개선:

- Redis 등에 인증 코드 저장
- TTL 3분 적용
- 코드 검증 API 추가
- 응답에서 validationCode 제거

---

### 12.5 승인 API 권한 검증 필요

`/api/v1/request/accounts/load-all`, `/approve`는 관리자성 API입니다.

현재 코드상 컨트롤러나 서비스에서 명시적인 `isAdmin` 검사가 보이지 않습니다.

개선:

- JWT claim 기반 role 검증
- `isAdmin=true` 사용자만 접근 허용
- Spring Security authorization rule 적용

---

### 12.6 Host 헤더 검증 방식 개선 필요

현재 `DomainFilter`는 `Host` 헤더가 정확히 `java-sso.opentofu.click`일 때만 통과합니다.

주의:

- 프록시/Ingress 환경에서 Host 처리 방식에 따라 정상 요청이 차단될 수 있음
- RuntimeException 대신 명확한 403 응답이 적합함

---

### 12.7 토큰/민감정보 로그 출력

필터에서 JWT 토큰 값을 로그로 출력합니다.

운영 위험:

- 로그 유출 시 인증 토큰 탈취 가능
- 보안 감사상 민감정보 로깅 이슈

개선:

- 토큰 전체 출력 제거
- 필요 시 앞/뒤 일부만 마스킹
- 인증 실패 사유 중심으로 로깅

---

### 12.8 `ddl-auto=update` 운영 사용 주의

운영 DB에서 `spring.jpa.hibernate.ddl-auto=update`는 의도치 않은 스키마 변경 리스크가 있습니다.

개선:

- Flyway 또는 Liquibase 도입
- 운영은 `validate` 권장

---

## 13. SRE 관점 운영 체크리스트

### 인증 안정성

- [ ] Access Token / Refresh Token TTL 정책 확정
- [ ] Refresh Token Rotation 적용
- [ ] 서버 재시작 후 JWT 검증 안정성 확보
- [ ] 다중 인스턴스 키 공유 구조 확보
- [ ] 로그에 JWT 원문 미출력

### 보안

- [ ] 모든 시크릿 외부화
- [ ] 관리자 API 권한 검증
- [ ] CSRF/CORS 정책 재검토
- [ ] 이메일 인증 코드 서버 측 검증
- [ ] Host Filter 403 응답 처리

### 데이터베이스

- [ ] `users.email_id` PK 확인
- [ ] `user_index` unique index 확인
- [ ] `db_table` 네이밍 정책 검증
- [ ] schema migration 도구 도입

### 운영성

- [ ] `/actuator/health` 추가 검토
- [ ] structured logging 적용
- [ ] login failure metric 수집
- [ ] token reissue metric 수집
- [ ] approval API audit log 적용

### Kubernetes/Cloud 운영

- [ ] Secret 기반 설정 주입
- [ ] readiness/liveness probe 구성
- [ ] CPU/Memory request/limit 설정
- [ ] HPA 기준 지표 정의
- [ ] TLS 종료 위치 결정

---

## 14. 예상 테이블 구조

JPA Entity 기준 예상 `users` 테이블은 다음 구조입니다.

```sql
CREATE TABLE users (
    email_id VARCHAR(255) PRIMARY KEY,
    uuid VARCHAR(255),
    password VARCHAR(255),
    is_enabled BOOLEAN,
    is_admin BOOLEAN,
    db_table VARCHAR(255),
    user_index INT UNIQUE
);
```

실제 DDL은 JPA naming strategy 및 MySQL dialect에 따라 달라질 수 있습니다.

---

## 15. 주요 실행 흐름 요약

```text
회원가입
  POST /sign-up
  -> email 중복 검사
  -> password BCrypt encode
  -> isEnabled=false 저장
  -> 승인 신청 메일 발송

로그인
  POST /login
  -> email/password 검증
  -> access/refresh token 생성
  -> HttpOnly Secure Cookie 설정

로그인 유지
  POST /login-check
  -> access token 확인
  -> refresh token 확인
  -> 필요 시 access token 재발급

승인
  POST /accounts/approve
  -> 사용자 조회
  -> isEnabled=true 저장

로그아웃
  POST /logout
  -> access/refresh cookie maxAge=0
```

---

## 16. 프로젝트 성격 요약

이 SSO 서버는 Terraform SaaS 플랫폼에서 독립 인증 서버 역할을 수행합니다. 현재 구조는 단순하고 이해하기 쉬우며, 쿠키 기반 SSO, JWT 공개키 제공, 가입 승인 플로우까지 포함하고 있습니다.

다만 운영 환경으로 가져가기 전에는 다음 보강이 중요합니다.

1. 시크릿 외부화
2. JWT 키 영속화
3. Refresh Token 저장/폐기 구조
4. 관리자 API 권한 검증
5. 이메일 인증 코드 서버 측 검증
6. 민감정보 로그 제거
7. DB migration 관리
8. Health Check 및 운영 지표 추가

