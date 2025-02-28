# 🚀 고양이 입양 프로젝트
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-lightblue)

## 🎯 프로젝트 개요
이 프로젝트는 DDD(Domain-Driven Design) 패턴을 적용하여 구성된 Spring Boot 기반 서버 어플리케이션
고양이를 입양하길 원하는 사람들을 연결하는 플랫폼
`JPA`, `Spring Security`, `Lombok` 등을 활용하고, `PostgreSQL`을 데이터베이스로 사용

## 📁 패키지 구조

```
src 
├── main
│   ├── java/com.matchhub.catconnect
│   │   ├── common
│   │   │   ├── controller
│   │   │   ├── enums                 # ErrorCode 정의
│   │   │   ├── model
│   │   │   │   ├── entity            # BaseEntity 정의
│   │   ├── domain
│   │   │   ├── board
│   │   │   │   ├── restcontroller    # API 컨트롤러 (”/api” prefix)
│   │   │   │   ├── model
│   │   │   │   │   ├── dto
│   │   │   │   │   ├── entity
│   │   │   │   │   ├── enums
│   │   │   │   │   ├── record
│   │   │   │   ├── repository
│   │   │   │   ├── service
│   │   │   ├── comment # board와 동일한 구조
│   │   │   ├── user # board와 동일한 구조
│   │   ├── global
│   │   │   ├── configuration         # JpaAuditingConfig
│   │   │   ├── exception             # 예외 처리 (GlobalExceptionHandler 등)
│   │   │   ├── response              # API Response 정의
│   │   │   ├── util
│   │   │   │   ├── auth
│   │   │   │   ├── mail
│   │   ├── CatconnectApplication          # SpringBootApplication 진입점
│   ├── resources
│   │   ├── static
│   │   ├── templates
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-local.yml
│   │   ├── application-prod.yml
```

## ⚙️ 기술 스택
- Lang: Java 21
- Framework: Spring Boot 3.4.2
- DB: PostgreSQL
- Build Tools: Gradle
- Library:
    - Spring Boot Starter (JPA, Security, Web, Mail)
    - Lombok

## 🚀 프로젝트 실행 방법
### 1️⃣ 클론
터미널을 실행하고, 아래 명령어를 실행

```sh
git clone https://github.com/tletle7102/catconnect.git
cd catconnect
```

- `git clone https://github.com/tletle7102/catconnect.git` 은 해당 깃허브 리포지토리(이하 origin)를 복제하여 터미널에 위치한 디렉토리에 설치하여 로컬 리포지토리를 생성하는 명령어
- `cd catconnect` 는 위 명령어를 실행시킨 디렉토리에서 로컬 리포지토리에 생성된 리포지토리 디렉토리로 이동하는 명령어

### 2️⃣ 환경변수 설정
프로젝트 루트 디렉토리에 `.env` 파일을 생성하고, 아래 내용을 입력
아래 내용에서 값이 없는 부분은 노출에 민감하기 때문에 사용 시, 채워 넣어야 함

```env
CATCONNECT_SPRING_PROFILE_ACTIVE= 
CATCONNECT_TOMCAT_PORT= 
CATCONNECT_SPRING_SECURITY_JWT_SECRET=
CATCONNECT_SPRING_SECURITY_EXPIRATION= 
CATCONNECT_LOCAL_DB_URL= 
CATCONNECT_LOCAL_DB_USERNAME= 
CATCONNECT_LOCAL_DB_PASSWORD= 
CATCONNECT_LOCAL_DB_NAME= 
CATCONNECT_DEV_DB_URL= 
CATCONNECT_DEV_DB_USERNAME= 
CATCONNECT_DEV_DB_PASSWORD= 
CATCONNECT_DEV_DB_NAME= 

```

### 3️⃣ 실행

아래 두 방법 중 한 가지 방법을 선택하여 실행(택 1)


#### ① 환경변수 파일을 명령어로 주입하여 Gradle로 스프링부트 실행

```sh
env $(grep -v '^#' dev.env | xargs) ./gradlew bootRun
```

#### ② 환경변수 파일을 리눅스 환경변수로 활용하여 스프링부트 빌드 후 실행

```sh
export $(grep -v '^#' dev.env | xargs) && ./gradlew clean build
java -jar build/libs/{프로젝트명}-0.0.1-SNAPSHOT.jar
```
