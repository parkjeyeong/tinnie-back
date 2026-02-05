# Repository Guidelines

## 프로젝트 개요
- AI로 루틴을 만들고 관리할 수 있는 프로젝트의 백엔드입니다.

## Project Structure & Module Organization
- `src/main/java/app/tinnie`: Spring Boot 애플리케이션 코드(컨트롤러, 서비스, 설정, 핸들러, 도메인 DTO).
- `src/main/resources`: `application.yaml` 등 런타임 설정 파일.
- `src/test/java`: JUnit 테스트(현재는 최소 수준).
- `build/`: Gradle 빌드 결과물과 컴파일 산출물(자동 생성).
- 루트: `build.gradle`, `settings.gradle`, `Dockerfile`, `docker-compose.yml`, Gradle wrapper 스크립트.

## Build, Test, and Development Commands
- `./gradlew bootRun`: 기본 프로파일로 Spring Boot 앱을 로컬 실행.
- `./gradlew test`: JUnit Platform 기반 테스트 실행.
- `./gradlew build`: 컴파일, 테스트, 패키징 수행.
- `./gradlew clean`: `build/` 산출물 제거.
- `docker-compose up`: `docker-compose.yml`에 정의된 사전 빌드 이미지 실행.

## Coding Style & Naming Conventions
- Java 17, Spring Boot 3.3.x + MyBatis + Lombok 기준; import 정리 및 미사용 import 제거.
- 들여쓰기는 기존 코드 스타일을 따름(2 spaces, no tabs).
- 패키지는 `app.tinnie.*`, 클래스는 `PascalCase`, 메서드/필드는 `camelCase` 사용.
- DTO는 `app.tinnie.domain.*.dto`에 두고, 컨트롤러 경로는 간결하게 유지.

## Testing Guidelines
- 프레임워크: JUnit 5 (`org.springframework.boot:spring-boot-starter-test`).
- 테스트 클래스는 보통 `Tests`로 끝남(예: `SimpleServerApplicationTests`).
- 새 기능에는 집중 테스트를 추가하고, 배선이 필요할 때만 `@SpringBootTest` 사용.

## Commit & Pull Request Guidelines
- 이 저장소에는 명시적 커밋 규칙이 없음; 간결한 명령형 제목 사용(예: "Add websocket room handler").
- PR에는 요약, 변경 이유, 테스트 결과, 설정 변경 사항을 포함.
- 관련 이슈를 링크하고 필요한 환경 변수/시크릿을 명시.

## Security & Configuration Tips
- `src/main/resources/application.yaml`에 실제 엔드포인트/자격 증명이 포함되어 있으므로 새 시크릿 커밋은 금지.
- 로컬/개발 환경에서는 DB 및 API 키를 환경 변수로 오버라이드 권장.
