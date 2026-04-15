# 1. JRE 이미지를 베이스로 사용 (실행 전용, JDK보다 경량)
FROM eclipse-temurin:17-jre-jammy

# 2. 비루트 사용자 생성 및 작업 디렉토리 설정
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app

# 3. 빌드된 JAR 파일 경로를 변수로 정의
ARG JAR_FILE=build/libs/*.jar

# 4. JAR 파일을 컨테이너 내부로 복사
COPY --chown=appuser:appuser ${JAR_FILE} app.jar

# 5. 비루트 사용자로 전환
USER appuser

# 6. 컨테이너 시작 시 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 7. 서비스 포트 노출 (Spring Boot 기본 8080)
EXPOSE 8080