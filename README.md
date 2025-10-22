# Profile Server

사용자 프로필 생성, 조회, 수정 및 검색을 관리하는 Spring Boot 마이크로서비스입니다.

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [주요 기능](#주요-기능)
3. [아키텍처](#아키텍처)
4. [데이터베이스 스키마](#데이터베이스-스키마)
5. [API 엔드포인트](#api-엔드포인트)
6. [기술 스택](#기술-스택)
7. [설정 및 실행](#설정-및-실행)
8. [배포](#배포)

---

## 프로젝트 개요

### 기본 정보

- **프로젝트명**: Profile Server
- **타입**: Spring Boot REST API 마이크로서비스
- **Java**: 21
- **빌드**: Gradle 8.x
- **버전**: 1.0.0-SNAPSHOT

### 핵심 목적

마이크로서비스 아키텍처 환경에서 사용자 프로필을 전담 관리하는 서버입니다.
- 사용자 프로필 생성 및 수정
- 다양한 조건의 프로필 검색 (지역, 장르, 악기, 닉네임 등)
- 배치 프로필 조회 (단순/상세)
- 변경 이력 추적
- 이벤트 기반 통합 (Kafka)

---

## 주요 기능

### 1. 프로필 관리
- 사용자 프로필 생성 (Kafka 이벤트 기반)
- 프로필 정보 수정 (닉네임, 소개, 지역, 성별 등)
- 닉네임 중복 검증
- 낙관적 락 (@Version)을 통한 동시성 제어

### 2. 사용자 속성 관리
- 장르(Genre) 다중 선택 및 관리
- 악기(Instrument) 다중 선택 및 관리
- 지역(Location) 설정
- 프로필 공개 여부 설정
- 채팅 가능 여부 설정

### 3. 프로필 검색 기능
- **단일 조회**: userId 기반 상세 프로필 조회
- **복합 검색**: 지역, 닉네임, 장르, 악기, 성별 조합 검색
- **커서 기반 페이징**: 무한 스크롤 지원 (Slice)
- **배치 조회**:
  - 단순 조회 (POST /simple/batch): userId, nickname, profileImageUrl만 반환
  - 상세 조회 (GET /detail/batch): 전체 프로필 정보 반환

### 4. QueryDSL 기반 동적 쿼리
- 복잡한 검색 조건을 동적으로 구성
- 인덱스 최적화를 통한 성능 향상
- 커서 기반 페이징으로 효율적인 대용량 데이터 처리

### 5. 이벤트 기반 통합
- Kafka 컨슈머를 통한 프로필 생성 요청 수신
- 프로필 변경 이벤트 발행:
  - `profile-image-changed`: 프로필 이미지 변경 시
  - `user-nickname-changed`: 닉네임 변경 시
  - `user-deleted`: 사용자 삭제 시
- 느슨한 결합 (Loose Coupling)을 통한 확장 가능한 구조

### 6. 변경 이력 추적
- 모든 프로필 변경사항을 History 테이블에 자동 기록
- 변경 필드, 이전 값, 새 값, 변경 시각 추적
- 감사(Audit) 및 디버깅 용도

### 7. 성능 최적화
- 복합 인덱스를 통한 검색 쿼리 최적화
- 커서 기반 페이징 (Slice)
- N+1 문제 방지 (Fetch Join)
- QueryDSL을 통한 쿼리 최적화

---

## 아키텍처

### 계층 구조

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (ProfileSearch, ProfileUpdate...)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  (ProfileSearchService, ProfileUpdate...)│
│  (UserInfoLifeCycleService)              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Event Layer (Kafka)                │
│  (ProfileCreateRequest, ImageChanged...) │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Repository Layer (JPA+QueryDSL)    │
│  (UserInfoRepository, ProfileSearch...)  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Entity Layer                    │
│  (UserInfo, UserGenres, UserInstruments) │
└─────────────────────────────────────────┘
```

### 디자인 패턴

#### 1. 이벤트 기반 아키텍처
- Kafka를 통한 비동기 이벤트 처리
- 프로필 생성 요청 수신 (Consumer)
- 프로필 변경 이벤트 발행 (Producer)
- 느슨한 결합 및 확장 가능한 구조

#### 2. Repository 패턴
- JPA Repository를 통한 기본 CRUD
- Custom Repository (QueryDSL)를 통한 복잡한 검색
- 계층 간 명확한 책임 분리

#### 3. DTO 패턴
- 요청/응답 객체 분리
- Entity와 API 계층 간 결합도 감소
- Mapper를 통한 변환 로직 캡슐화

#### 4. Builder 패턴
- Entity 및 DTO 생성 시 가독성 향상
- Lombok @Builder 활용

#### 5. 낙관적 락 (Optimistic Locking)
- @Version을 통한 동시성 제어
- 충돌 감지 및 재시도 메커니즘

---

## 데이터베이스 스키마

### 핵심 엔티티

#### 1. user_info (사용자 정보)
```sql
user_id             VARCHAR(255) PRIMARY KEY  -- 사용자 고유 ID
profile_image_url   VARCHAR(500)              -- 프로필 이미지 URL
sex                 CHAR(1)                   -- 성별 (M/F/O)
nickname            VARCHAR(100) UNIQUE       -- 닉네임 (중복 불가)
city                VARCHAR(100)              -- 지역
introduction        TEXT                      -- 자기소개
version             INT DEFAULT 0             -- 낙관적 락 버전
created_at          TIMESTAMP                 -- 생성일
last_updated_at     TIMESTAMP                 -- 수정일
is_public           BOOLEAN DEFAULT TRUE      -- 공개 여부
is_chatable         BOOLEAN DEFAULT TRUE      -- 채팅 가능 여부
```

#### 2. location_names (지역 명칭)
```sql
city_id     VARCHAR(50) PRIMARY KEY   -- 지역 코드 (SEOUL, BUSAN 등)
city_name   VARCHAR(100)              -- 지역 한글명
```

#### 3. genre_name (장르 명칭)
```sql
genre_id    INT PRIMARY KEY   -- 장르 ID
genre_name  VARCHAR(100)      -- 장르명 (ROCK, JAZZ 등)
version     INT DEFAULT 0     -- 낙관적 락 버전
```

#### 4. instrument_name (악기 명칭)
```sql
instrument_id    INT PRIMARY KEY   -- 악기 ID
instrument_name  VARCHAR(100)      -- 악기명 (GUITAR, DRUM 등)
version          INT DEFAULT 0     -- 낙관적 락 버전
```

#### 5. user_genres (사용자-장르 매핑)
```sql
user_id     VARCHAR(255)  -- FK to user_info
genre_id    INT           -- FK to genre_name
version     INT DEFAULT 0
PRIMARY KEY (user_id, genre_id)
```

#### 6. user_instruments (사용자-악기 매핑)
```sql
user_id         VARCHAR(255)  -- FK to user_info
instrument_id   INT           -- FK to instrument_name
version         INT DEFAULT 0
PRIMARY KEY (user_id, instrument_id)
```

#### 7. profile_update_history (프로필 변경 이력)
```sql
history_id   BIGINT AUTO_INCREMENT PRIMARY KEY
user_id      VARCHAR(255)  -- FK to user_info
field_name   VARCHAR(100)  -- 변경된 필드명
old_val      TEXT          -- 변경 전 값
new_val      TEXT          -- 변경 후 값
updated_at   TIMESTAMP     -- 변경 시각
```

### ERD

```
┌──────────────────┐
│  location_names  │
│──────────────────│
│ city_id (PK)     │
│ city_name        │
└──────────────────┘

┌──────────────────┐
│   genre_name     │
│──────────────────│
│ genre_id (PK)    │──┐
│ genre_name       │  │
└──────────────────┘  │
                      │
┌──────────────────┐  │
│ instrument_name  │  │
│──────────────────│  │
│ instrument_id(PK)│──┼┐
│ instrument_name  │  ││
└──────────────────┘  ││
                      ││
┌──────────────────┐  ││
│    user_info     │  ││
│──────────────────│  ││
│ user_id (PK)     │◄─┼┼┐
│ nickname         │  │││
│ city             │  │││
│ sex              │  │││
│ version          │  │││
└─────┬────────────┘  │││
      │ 1:N           │││
      ▼               │││
┌──────────────────┐  │││
│  user_genres     │  │││
│──────────────────│  │││
│ user_id (PK/FK)  │──┘││
│ genre_id (PK/FK) │◄──┘│
└──────────────────┘    │
                        │
      │ 1:N             │
      ▼                 │
┌──────────────────┐    │
│ user_instruments │    │
│──────────────────│    │
│ user_id (PK/FK)  │────┘
│ instrument_id(PK)│◄───┘
└──────────────────┘

      │ 1:N
      ▼
┌──────────────────────┐
│ profile_update_      │
│ history              │
│──────────────────────│
│ history_id (PK)      │
│ user_id (FK)         │
│ field_name           │
│ old_val              │
│ new_val              │
└──────────────────────┘
```

### 인덱스 전략

**user_info 테이블:**
- `idx_user_info_nickname`: 닉네임 검색
- `idx_user_info_city`: 지역 필터링
- `idx_user_info_is_public`: 공개 여부 필터링
- `idx_user_info_composite_search`: 복합 검색 (city, sex, is_public)

**profile_update_history 테이블:**
- `idx_history_user_id`: 사용자별 이력 조회
- `idx_history_composite`: (user_id, updated_at DESC) 복합 인덱스

**user_genres / user_instruments:**
- 역방향 조회 인덱스 (장르→사용자, 악기→사용자)

---

## API 엔드포인트

### 프로필 조회

#### GET /api/profiles/profiles/{userId}
**단일 프로필 상세 조회**

```http
Response:
{
  "userId": "user123",
  "nickname": "음악인",
  "profileImageUrl": "https://example.com/profile.jpg",
  "sex": "M",
  "city": "서울",
  "introduction": "안녕하세요",
  "isPublic": true,
  "isChatable": true,
  "genres": [
    {"genreId": 1, "genreName": "ROCK"},
    {"genreId": 2, "genreName": "JAZZ"}
  ],
  "instruments": [
    {"instrumentId": 1, "instrumentName": "GUITAR"}
  ],
  "createdAt": "2025-10-22T10:00:00",
  "updatedAt": "2025-10-22T12:00:00"
}
```

#### GET /api/profiles/profiles
**복합 조건 프로필 검색 (커서 기반 페이징)**

```http
Query Parameters:
- city: String (옵션) - 지역 필터
- nickName: String (옵션) - 닉네임 부분 매칭
- genres: List<Integer> (옵션) - 장르 ID 리스트
- instruments: List<Integer> (옵션) - 악기 ID 리스트
- sex: Character (옵션) - 성별 (M/F/O)
- cursor: String (옵션) - 페이징 커서
- size: int (기본값: 10, 최대: 100)

Response:
{
  "content": [...],
  "hasNext": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 10
}
```

#### POST /api/profiles/profiles/simple/batch
**배치 단순 프로필 조회**

```http
Content-Type: application/json

Request Body:
["user123", "user456", "user789"]

Response:
[
  {
    "userId": "user123",
    "nickname": "음악인",
    "profileImageUrl": "https://example.com/profile.jpg"
  },
  ...
]
```

#### GET /api/profiles/profiles/detail/batch
**배치 상세 프로필 조회**

```http
Query Parameters:
- userIds: List<String> (필수)

Response: List<UserResponse> (전체 프로필 정보)
```

### 프로필 수정

#### PUT /api/profiles/profiles/{userId}
**프로필 정보 수정**

```http
Content-Type: application/json

Request Body:
{
  "nickname": "새닉네임",
  "introduction": "새로운 소개",
  "city": "부산",
  "sex": "M",
  "isPublic": true,
  "isChatable": true,
  "genres": [1, 2, 3],
  "instruments": [1, 2]
}

Response:
{
  "success": true
}
```

### 닉네임 검증

#### GET /api/profiles/nickname/check
**닉네임 중복 확인**

```http
Query Parameters:
- nickname: String (필수)

Response:
{
  "available": true,
  "message": "사용 가능한 닉네임입니다"
}
```

### Enums 조회

#### GET /api/profiles/enums/genres
**장르 목록 조회**

```json
{
  "1": "ROCK",
  "2": "JAZZ",
  "3": "CLASSICAL"
}
```

#### GET /api/profiles/enums/instruments
**악기 목록 조회**

```json
{
  "1": "GUITAR",
  "2": "PIANO",
  "3": "DRUM"
}
```

#### GET /api/profiles/enums/locations
**지역 목록 조회**

```json
{
  "SEOUL": "서울",
  "BUSAN": "부산"
}
```

### 헬스 체크

#### GET /health
```
200 OK
"Server is up"
```

---

## 기술 스택

### Core
- **Spring Boot**: 3.5.5
- **Java**: 21 (Eclipse Temurin)
- **Gradle**: 8.x

### Database
- **Production**: MariaDB 10.11
- **Test**: H2 (in-memory)
- **JPA**: Hibernate
- **QueryDSL**: 5.0.0 (동적 쿼리)

### Messaging
- **Kafka**: spring-kafka
- **이벤트 처리**: Consumer/Producer

### Development
- **Lombok**: 코드 간소화
- **Validation**: Jakarta Validation
- **Slf4j**: 로깅

### Testing
- **JUnit 5**
- **Spring Boot Test**
- **@DataJpaTest**

---

## 설정 및 실행

### 로컬 실행 (dev 프로파일)

```bash
# 1. 환경 변수 설정
export SPRING_PROFILES_ACTIVE=dev

# 2. 데이터베이스 준비
mysql -u root -p < src/main/resources/sql/schema.sql
mysql -u root -p < src/main/resources/sql/Data.sql

# 3. Docker Compose로 인프라 시작 (Kafka, MariaDB, Redis)
docker-compose up -d

# 4. 실행
./gradlew bootRun
```

### 설정 파일

#### application.yaml
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

#### application-dev.yaml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:4000/profile
    username: root
    password: password

  kafka:
    bootstrap-servers: localhost:29091,localhost:29092,localhost:29093
    consumer:
      group-id: profile-service-group
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

#### application-prod.yaml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
    username: ${DATABASE_USER_NAME}
    password: ${DATABASE_PASSWORD}

  kafka:
    bootstrap-servers:
      - ${KAFKA_URL1}
      - ${KAFKA_URL2}
      - ${KAFKA_URL3}
```

---

## 배포

### Docker Compose

#### 아키텍처
```
┌─────────────────┐
│ Profile Server  │
└────────┬────────┘
         │
    ┌────┴────┬────────┐
    │         │        │
┌───▼──┐  ┌───▼──┐  ┌──▼───┐
│Kafka │  │Kafka │  │Kafka │
│  1   │  │  2   │  │  3   │
└──────┘  └──────┘  └──────┘
    │         │        │
    └─────────┼────────┘
              │
    ┌─────────▼─────────┐
    │   Zookeeper       │
    └───────────────────┘
              │
    ┌─────────▼─────────┐
    │   MariaDB         │
    │   Redis           │
    └───────────────────┘
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka-1:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "29091:29091"
      - "9091:9091"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3

  mariadb:
    image: mariadb:10.11
    ports:
      - "4000:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${DATABASE_PASSWORD}
      MYSQL_DATABASE: ${DATABASE_NAME}
    volumes:
      - mariadb-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mariadb-data:
  redis-data:
```

#### Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 배포 단계

```bash
# 1. 빌드
./gradlew clean build

# 2. Docker 이미지 생성
docker build -t profile-server:1.0.0 .

# 3. 인프라 시작
docker-compose up -d

# 4. 애플리케이션 실행
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_HOST=mariadb \
  -e KAFKA_URL1=kafka-1:9091 \
  --network profile-network \
  profile-server:1.0.0
```

---

## 프로젝트 구조

```
src/main/java/com/teambind/profileserver/
├── controller/
│   ├── ProfileSearchController.java
│   ├── ProfileUpdateController.java
│   ├── NicknameValidator.java
│   ├── EnumsController.java
│   └── HealthCheckController.java
│
├── service/
│   ├── create/
│   │   └── UserInfoLifeCycleService.java
│   ├── search/
│   │   └── ProfileSearchService.java
│   └── update/
│       └── ProfileUpdateService.java
│
├── repository/
│   ├── UserInfoRepository.java
│   ├── ProfileSearchRepository.java
│   ├── GenreNameTableRepository.java
│   ├── InstrumentNameTableRepository.java
│   ├── LocationNameTableRepository.java
│   ├── HistoryRepository.java
│   └── dsl/
│       └── ProfileSearchRepositoryImpl.java
│
├── entity/
│   ├── UserInfo.java
│   ├── History.java
│   ├── attribute/
│   │   ├── UserGenres.java
│   │   ├── UserInstruments.java
│   │   ├── key/
│   │   │   ├── UserGenreKey.java
│   │   │   └── UserInstrumentKey.java
│   │   └── nameTable/
│   │       ├── GenreNameTable.java
│   │       ├── InstrumentNameTable.java
│   │       └── LocationNameTable.java
│   └── ...
│
├── events/
│   ├── event/
│   │   ├── Event.java
│   │   ├── ProfileCreateRequest.java
│   │   ├── ProfileImageChanged.java
│   │   ├── UserDeletedEvent.java
│   │   └── UserNickNameChangedEvent.java
│   ├── consumer/
│   │   └── KafkaConsumer.java
│   └── producer/
│       └── KafkaProducer.java
│
├── dto/
│   ├── request/
│   │   └── ProfileUpdateRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── BatchUserSummaryResponse.java
│
└── config/
    ├── QuerydslConfig.java
    └── GenerateKeyConfig.java

src/main/resources/
├── application*.yaml
└── sql/
    ├── schema.sql
    ├── Data.sql
    ├── migration.sql
    └── sample-data.sql
```

---

## 주요 기능 상세

### 1. 프로필 생성 (이벤트 기반)

```
┌────────────┐      profile-create-request      ┌─────────────┐
│Auth Server │──────────────────────────────────>│   Kafka     │
└────────────┘                                   └──────┬──────┘
                                                        │
                                                        │ consume
                                                        │
                                                 ┌──────▼──────┐
                                                 │   Profile   │
                                                 │   Server    │
                                                 └─────────────┘
```

### 2. 프로필 검색 (QueryDSL)

- **동적 쿼리 생성**: 검색 조건에 따라 쿼리 동적 구성
- **커서 기반 페이징**: 대용량 데이터 효율적 처리
- **인덱스 최적화**: 복합 인덱스를 통한 성능 향상

### 3. 변경 이력 추적

```java
@Transactional
public void updateProfile(String userId, ProfileUpdateRequest request) {
    UserInfo userInfo = findUser(userId);

    // 변경 전 값 저장
    History history = History.builder()
        .userInfo(userInfo)
        .fieldName("nickname")
        .oldValue(userInfo.getNickname())
        .newValue(request.getNickname())
        .build();

    // 업데이트 및 이력 저장
    userInfo.setNickname(request.getNickname());
    historyRepository.save(history);
}
```

### 4. 이벤트 발행

```java
// 닉네임 변경 시
UserNickNameChangedEvent event = UserNickNameChangedEvent.builder()
    .userId(userId)
    .oldNickname(oldNickname)
    .newNickname(newNickname)
    .build();

kafkaProducer.send("user-nickname-changed", event);
```

---

## 성능 최적화

### 1. 인덱스 전략
- 복합 인덱스: (city, sex, is_public)
- 커버링 인덱스 활용
- 정렬 인덱스 (updated_at DESC)

### 2. QueryDSL 최적화
- Fetch Join으로 N+1 방지
- 동적 쿼리로 불필요한 조건 제거
- 프로젝션을 통한 필요 컬럼만 조회

### 3. 캐싱 전략
- Redis를 통한 자주 조회되는 데이터 캐싱
- NameTable (장르, 악기, 지역) 캐싱

### 4. 배치 처리
- 단순 배치 조회: 최소 정보만 반환
- IN 쿼리를 통한 효율적 조회

---

## 문서

- **작성일**: 2025-10-23
- **버전**: 1.0.0-SNAPSHOT
- **저자**: DDING
