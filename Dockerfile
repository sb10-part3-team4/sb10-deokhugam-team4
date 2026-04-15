# 1. JRE 이미지를 베이스로 사용 (실행 전용, JDK보다 경량)
FROM eclipse-temurin:17-jre-jammy

# 2. 빌드된 JAR 파일 경로를 변수로 정의
ARG JAR_FILE=build/libs/*.jar

# 3. JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} app.jar

# 4. 컨테이너 시작 시 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 5. 서비스 포트 노출 (Spring Boot 기본 8080)
EXPOSE 8080