# 1. JDK 17 기반 이미지를 사용
FROM eclipse-temurin:17-jdk-alpine AS build

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일을 컨테이너에 복사
COPY ./build/libs/tinnie-back-0.0.1-SNAPSHOT.jar /app/app.jar

# 4. 컨테이너에서 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]