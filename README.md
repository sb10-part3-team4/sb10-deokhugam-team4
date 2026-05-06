# 덕후감 (Deokhugam)

## 로컬 개발 환경 설정

### 1. 인프라 실행

Docker Compose로 인프라를 실행합니다.

```bash
docker compose up -d
```

- PostgreSQL: `localhost:5433` (ID: deokhugam / PW: deokhugam / DB: deokhugam)
- Redis: `localhost:6379`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin / admin)

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

### 4. Grafana 설정

Grafana 데이터소스 설정:
1. `http://localhost:3000` 접속 → admin / admin 로그인
2. Configuration → Data Sources → Add data source
3. Prometheus 선택 → URL에 `http://prometheus:9090` 입력 → Save & Test

Spring Boot 메트릭 엔드포인트: `http://localhost:8080/actuator/prometheus`

### 5. 테스트

```bash
./gradlew test
```

테스트는 Testcontainers를 사용하므로 Docker가 실행 중이어야 합니다.
