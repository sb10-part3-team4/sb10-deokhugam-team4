# 덕후감 - Chicken Squad
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
 <img width="1432" height="1264" alt="image" src="https://github.com/user-attachments/assets/0ed2ac19-e1d5-4e33-8cc7-64a6ca61c40d" />
 <img width="1460" height="1186" alt="image" src="https://github.com/user-attachments/assets/b3b71e61-67f1-40c3-9a55-1311a47c8f8a" />

* #### 리뷰
  * 리뷰 관련 CRUD...
* #### 대시보드
  * 리뷰 관련 CRUD...

### 김진우
(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)
* #### 사용자
    * 사용자 관련 CRUD...

### 임지호
(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)
* #### 댓글
    * 댓글 관련 CRUD...

### 정수현
* #### 도서
* <img width="600" alt="도서등록" src="https://github.com/user-attachments/assets/24c28620-db3f-4c37-8585-b08af8fbdbe3" />
* <img width="600" alt="도서수정" src="https://github.com/user-attachments/assets/491716b8-baf9-45f6-8a49-9a5f77a45a4e" />
* <img width="600" alt="도서조회" src="https://github.com/user-attachments/assets/9a644374-8c6a-46df-8028-b8bcbbc588db" />
   
* Book 도메인 
  * Book 엔티티 설계
  * 도서 CRUD API 구현 (생성 / 조회 / 수정 / 삭제 / 영구삭제)
  * 커서 기반 페이지네이션 목록 조회 (jOOQ 활용)
  * 네이버 Book API 연동 (ISBN 검색)
  * OCR.space API 연동 (이미지에서 ISBN 자동 추출 → 도서 정보 반환) — PR #170
  * AWS S3 썸네일 업로드 (파일 타입 검증, TransactionSynchronizationManager로 S3-DB 정합성 처리) — PR #164
  * 분산락 적용 (createBook / updateBook / deleteBook에 @DistributedLock)
    인프라 & CI/CD
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
(제작한 발표자료 링크 혹은 첨부파일 첨부)
***
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
