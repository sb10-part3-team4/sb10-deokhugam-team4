# 덕후감 - Chicken Squad

[![codecov](https://codecov.io/gh/sb10-part3-team4/sb10-deokhugam-team4/branch/dev/graph/badge.svg)](https://codecov.io/gh/sb10-part3-team4/sb10-deokhugam-team4)

팀 노션 : https://www.notion.so/sb10-part3-team4-e4147b8a4b99836aab2e81f37c1a51b6<br>
팀 깃허브 : https://github.com/sb10-part3-team4/sb10-deokhugam-team4

## 팀원 구성
* 송시연(https://github.com/dstle)
* 김진우(https://github.com/zinuzanu)
* 임지호(https://github.com/jiho0420)
* 정수현(https://github.com/JeongSooHyeon)
***

## 프로젝트 소개
* 도서 OCR ISBN 인식부터 리뷰, 댓글, 알림, 인기 도서 대시보드까지 제공하는 도서 커뮤니티 플랫폼
* 프로젝트 기간 : 2026.04.14 ~ 2026.05.08
***

## 기술 스택
* Backend: Java 17, Spring Boot, Spring Data JPA, JOOQ, MapStruct, Flyway, Testcontainers
* Database: PostgreSQL
* Cache & 분산락 : Redis(AWS ElastiCache Valkey, Redisson)
* Infrastructure: AWS ECS, AWS ECR, AWS RDS, AWS S3
* CI/CD: GitHub Actions
* 공통 Tool: Git & GitHub, Discord, Notion, CodeRabbit
***

## 팀원별 구현 기능 상세

### 송시연
<img width="600" alt="image" src="https://github.com/user-attachments/assets/b9a5a79e-6039-40f1-aa5c-258c42c4939b" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/e28dfca7-bc04-4652-9a34-cce0be20c641" />


* Dashboard 도메인
    * 인기 도서 / 인기 리뷰 / 파워 유저 집계
    * jOOQ로 점수 산식 SQL 빌드, LIMIT N으로 상위만 저장
* Review 도메인
    * 리뷰 CRUD + 좋아요 토글 API
    * 동일 (도서, 사용자) 중복 리뷰 차단 (unique 부분 인덱스 + 분산락 이중 방어)
    * fetch join으로 N+1 제거
* 공통 / 인프라
    * 이벤트 기반 도서 통계 (동기, AFTER_COMMIT): 리뷰 CUD 시 Spring Event 발행 → `@TransactionalEventListener(AFTER_COMMIT)`으로 같은 스레드에서 동기 처리, 트랜잭션 롤백 시 통계도 함께 롤백
    * Redis 분산락: `@DistributedLock` 어노테이션 + AOP, SpEL 기반 키 추출. `@Order`로 락 어드바이스를 `@Transactional` 바깥에 배치해 락 해제 시점이 커밋 + AFTER_COMMIT 리스너 종료 시점이 되도록 보장
    * Spring Batch: 대시보드 배치를 12 Job(기간 4 × 종류 3)으로 분리. 1시간 cron + 최근 30일 실패 날짜 자동 재시도, 성공 Job 자동 스킵
    * jOOQ + Flyway + Testcontainers 세팅: Testcontainers로 PostgreSQL을 띄워 Flyway 마이그레이션 후 jOOQ 코드 자동 생성 (`generateJooq` 태스크)
    * 모니터링: 운영 -> CloudWatch (Micrometer cloudwatch2 registry), 로컬 -> docker-compose로 Grafana + Prometheus
    * k6 부하 테스트: 리뷰 동시 생성 시나리오로 분산락 동작·p95 검증
    * P6Spy: 로컬 SQL 로깅 (`developmentOnly`로 prod jar 제외)

### 김진우
<img width="600" alt="image" src="https://github.com/user-attachments/assets/4da3f6a6-a8dd-4e29-b04c-1e4762c1564a" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/a104afe3-7789-4f18-87c1-043612fc62cb" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/3e48675e-996b-41a9-bc74-5fa40c7f968b" />


* User 도메인
  * 사용자 도메인 설계 및 CRUD (회원가입 / 로그인 / 조회 / 수정 / 논리 삭제 / 물리 삭제)
  * PasswordEncoder 기반 비밀번호 암호화 저장 및 검증 로직 구현
  * 이메일 중복 검증 및 이메일·닉네임·비밀번호 입력값 유효성 검사 로직 적용
  * 헤더(Deokhugam-Request-User-ID) 기반 인증 처리 및 LoginUser ArgumentResolver 적용
  * 논리 삭제(DELETED 상태 전환) 후 24시간 경과 시 `@Scheduled` cron 기반 물리 삭제 배치 구현
  * Redis 분산락 기반 회원가입 동시성 제어 적용
  * jOOQ 기반 만료 사용자 물리 삭제 쿼리 구현

* Notification 도메인
  * 알림 도메인 설계 및 CRUD (알림 목록 조회 / 읽음 처리 / 전체 읽음 처리)
  * 좋아요 / 댓글 / 인기 리뷰 랭킹(기간별 TOP 10) 이벤트 기반 알림 생성 로직 구현
  * jOOQ 기반 커서 페이지네이션 알림 목록 조회 구현 (최근순 정렬, CREATED_AT 커서)
  * 확인 후 1주일 경과한 알림 `@Scheduled` cron 기반 자동 삭제 배치 구현

* 이벤트 / 비동기 처리
  * ApplicationEventPublisher 기반 도메인 이벤트 발행 구조 설계
  * `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` 기반 비동기 알림 이벤트 처리 구현
  * 알림 저장 로직에 `REQUIRES_NEW` 적용으로 독립 트랜잭션 보장
  * ThreadPoolTaskExecutor 기반 비동기 스레드풀 설정 및 거절 정책 적용

### 임지호
<img width="600" alt="image" src="https://github.com/user-attachments/assets/33fb5d67-7a4e-4b7c-9952-b64d69c9b069" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/39b063cc-f1c2-40ce-bdc3-3bca737559e4" />

* 댓글
    * 댓글 도메인 설계 및 CRUD API 구현 (생성 / 조회 / 수정 / 논리·물리 삭제)
    * 댓글 단건/목록 조회 API 및 커서 기반 페이지네이션 구현 (jOOQ 활용)
    * 댓글 수 갱신 로직 구현 (Redis 분산 락 적용)
    * 댓글 생성/삭제 카운트 로직 구현 (이벤트 기반 아키텍처 도입)
    * Comment 도메인 단위 테스트 커버리지 보강

* 로그
    * 커스텀 로깅 필터 구현 (고유 요청 Trace ID 생성 및 IP 주소 트래킹)
    * 응답 헤더 내 요청 ID 정보 추가 및 로그 레벨·출력 형식 정의
    * 일별 로그 AWS S3 자동 백업 및 적재 파이프라인 구축 (@Scheduled 활용)

### 정수현
<img width="600" alt="도서등록" src="https://github.com/user-attachments/assets/24c28620-db3f-4c37-8585-b08af8fbdbe3" />
<img width="600" alt="도서수정" src="https://github.com/user-attachments/assets/491716b8-baf9-45f6-8a49-9a5f77a45a4e" />
<img width="600" alt="도서조회" src="https://github.com/user-attachments/assets/9a644374-8c6a-46df-8028-b8bcbbc588db" />

* Book 도메인
    * Book 엔티티 설계
    * 도서 CRUD API 구현 (생성 / 조회 / 수정 / 삭제 / 영구삭제)
    * 커서 기반 페이지네이션 목록 조회 (jOOQ 활용)
    * 네이버 Book API 연동 (ISBN 검색)
    * OCR.space API 연동 (이미지에서 ISBN 자동 추출 → 도서 정보 반환) — PR #170
    * AWS S3 썸네일 업로드 (파일 타입 검증, TransactionSynchronizationManager로 S3-DB 정합성 처리) — PR #164
    * 분산락 적용 (createBook / updateBook / deleteBook에 @DistributedLock)
* 인프라 & CI/CD
    * GitHub Actions CI 파이프라인 구축 (dev PR 시 테스트 + JaCoCo 80% 커버리지 검증)
    * GitHub Actions CD 파이프라인 구축 (main push 시 ECR 빌드 → ECS 자동 배포)
    * AWS ECS / EC2 인프라 구성 (Fargate → EC2 전환, IAM 역할 설정, ecs-task-def 관리)
    * ElastiCache Redis 연동 전환 (EC2 직접 설치 → Valkey ElastiCache로 변경)
    * CodeRabbit AI 코드리뷰 설정 (.coderabbit.yaml, 리뷰 규칙 작성)
***

## 파일 구조
```
├── k6
├── monitoring
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── codeit
    │   │           └── team4
    │   │               └── deokhugam
    │   │                   ├── book
    │   │                   ├── comment
    │   │                   ├── dashboard
    │   │                   ├── global
    │   │                   ├── naver
    │   │                   ├── notification
    │   │                   ├── ocr
    │   │                   ├── review
    │   │                   ├── s3
    │   │                   └── user
    │   └── resources
    │       └── db
    │           └── migration
    └── test
        ├── java
        │   └── com
        │       └── codeit
        │           └── team4
        │               └── deokhugam
        │                   ├── book
        │                   ├── comment
        │                   ├── config
        │                   ├── dashboard
        │                   ├── global
        │                   ├── notification
        │                   ├── ocr
        │                   ├── review
        │                   ├── s3
        │                   └── user
        └── resources
```
***

## 구현 홈페이지
http://3.37.86.22:8080
***

## 프로젝트 회고록
[발표_자료.pdf](https://drive.google.com/file/d/1Si3HqnK3iX5zU3PLZK1UhqcXWp0in7n_/view?usp=drive_link)
***


## 로컬 개발 환경 설정

### 1. 인프라 실행

Docker Compose로 PostgreSQL과 Redis를 실행합니다.

```bash
docker compose up -d
```

- PostgreSQL: `localhost:5433` (ID: deokhugam / PW: deokhugam / DB: deokhugam)
- Redis: `localhost:6379`

데이터는 `postgres-data` 볼륨에 유지됩니다.
<br/>
다만 기존에 Docker 단독 컨테이너로 사용하던 볼륨과는 별개이므로, 기존 데이터는 유지되지 않습니다.
<br/>
최초 또는 미적용 마이그레이션 적용 시 스키마(테이블)가 생성/변경될 수 있습니다.

볼륨까지 삭제하려면:

```bash
docker compose down -v
```

### 2. 애플리케이션 실행

IntelliJ에서 `DeokhugamApplication`을 실행합니다. (기본 프로필이 `local`입니다)

### 3. 기존 Docker 단독 컨테이너에서 전환

기존에 PostgreSQL을 단독 컨테이너로 사용하던 경우:

```bash
# 기존 컨테이너 정지/삭제
docker stop <기존_컨테이너명>
docker rm <기존_컨테이너명>

# Docker Compose로 전환
docker compose up -d

# 아래부터 원한다면
# 볼륨 확인
docker volume ls

# 볼륨 삭제
docker volume rm <볼륨명>
```

Flyway가 스키마를 자동 생성하므로 앱 실행 시 테이블이 만들어집니다.

### 4. 테스트

```bash
./gradlew test
```

테스트는 Testcontainers를 사용하므로 Docker가 실행 중이어야 합니다.
