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
(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)
* #### 리뷰
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
(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)
* #### 도서
    * 도서 관련 CRUD...
***

## 파일 구조
```
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┣ com
 ┃ ┃ ┃ ┣ example
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┣ AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┣ UserController.java
 ┃ ┃ ┃ ┃ ┃ ┗ AdminController.java
 ┃ ┃ ┃ ┃ ┣ model
 ┃ ┃ ┃ ┃ ┃ ┣ User.java
 ┃ ┃ ┃ ┃ ┃ ┗ Course.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┣ UserRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ CourseRepository.java
 ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┣ AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┣ UserService.java
 ┃ ┃ ┃ ┃ ┃ ┗ AdminService.java
 ┃ ┃ ┃ ┃ ┣ security
 ┃ ┃ ┃ ┃ ┃ ┣ SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┗ JwtAuthenticationEntryPoint.java
 ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┣ LoginRequest.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserResponse.java
 ┃ ┃ ┃ ┃ ┣ exception
 ┃ ┃ ┃ ┃ ┃ ┣ GlobalExceptionHandler.java
 ┃ ┃ ┃ ┃ ┃ ┗ ResourceNotFoundException.java
 ┃ ┃ ┃ ┃ ┣ utils
 ┃ ┃ ┃ ┃ ┃ ┣ JwtUtils.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserMapper.java
 ┃ ┃ ┃ ┣ resources
 ┃ ┃ ┃ ┃ ┣ application.properties
 ┃ ┃ ┃ ┃ ┗ static
 ┃ ┃ ┃ ┃ ┃ ┣ css
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
 ┃ ┃ ┃ ┃ ┃ ┣ js
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
 ┃ ┃ ┃ ┣ webapp
 ┃ ┃ ┃ ┃ ┣ WEB-INF
 ┃ ┃ ┃ ┃ ┃ ┗ web.xml
 ┃ ┃ ┃ ┣ test
 ┃ ┃ ┃ ┃ ┣ java
 ┃ ┃ ┃ ┃ ┃ ┣ com
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ example
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserControllerTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ApplicationTests.java
 ┃ ┃ ┃ ┣ resources
 ┃ ┃ ┃ ┃ ┣ application.properties
 ┃ ┃ ┃ ┃ ┗ static
 ┃ ┃ ┃ ┃ ┃ ┣ css
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
 ┃ ┃ ┃ ┃ ┃ ┣ js
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
 ┣ pom.xml
 ┣ Application.java
 ┣ application.properties
 ┣ .gitignore
 ┗ README.md

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
