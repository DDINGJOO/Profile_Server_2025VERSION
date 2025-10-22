# Profile Server 향후 구현 계획 보고서

> 작성일: 2025-10-20
> 대상: Profile Server 리팩토링 및 신규 기능 개발

---

## 📑 목차

1. [개요](#개요)
2. [Phase 1: 객체지향 설계 개선](#phase-1-객체지향-설계-개선)
3. [Phase 2: 검증 구조 현대화](#phase-2-검증-구조-현대화)
4. [Phase 3: 코드 품질 개선](#phase-3-코드-품질-개선)
5. [Phase 4: 아키텍처 개선](#phase-4-아키텍처-개선)
6. [Phase 5: 신규 기능 추가](#phase-5-신규-기능-추가)
7. [구현 우선순위 매트릭스](#구현-우선순위-매트릭스)
8. [예상 일정 및 리소스](#예상-일정-및-리소스)

---

## 개요

### 목적
이 문서는 Profile Server의 코드 품질과 아키텍처를 개선하고, 신규 기능을 추가하기 위한 구체적인 실행 계획을 제시합니다.

### 핵심 목표
1. **객체지향 설계 원칙 준수**: SOLID 원칙에 부합하는 설계로 개선
2. **검증 구조 현대화**: Bean Validation 기반 커스텀 어노테이션 도입
3. **코드 중복 제거**: DRY 원칙 준수 및 유지보수성 향상
4. **확장 가능한 아키텍처**: 도메인 중심 설계로 전환

### 성공 지표
- 코드 중복률: 현재 약 30% → 목표 5% 이하
- 테스트 커버리지: 현재 약 40% → 목표 80% 이상
- 평균 메서드 길이: 현재 50줄 → 목표 20줄 이하
- API 응답 시간: 현재 유지 (성능 저하 없이 개선)

---



## Phase 3: 코드 품질 개선

### 3.2 Service 계층 리팩토링 - Template Method 패턴 적용

#### 목표
악기/장르 업데이트 로직의 중복 제거

#### 현재 문제점


#### 개선안

**Step 1: 공통 인터페이스 정의**

`src/main/java/com/teambind/profileserver/service/strategy/AttributeUpdateStrategy.java`

```java
package com.teambind.profileserver.service.strategy;

import com.teambind.profileserver.entity.UserInfo;
import java.util.List;
import java.util.Set;

public interface AttributeUpdateStrategy<T> {
    /**
     * 현재 속성 ID 목록 조회
     */
    List<Integer> findCurrentIds(String userId);

    /**
     * 속성 삭제
     */
    void deleteByIds(String userId, Set<Integer> idsToRemove);

    /**
     * 속성 추가
     */
    void addAttributes(UserInfo userInfo, Set<Integer> idsToAdd);
}
```

**Step 2: Template Method 구현**

`src/main/java/com/teambind/profileserver/service/template/AttributeUpdateTemplate.java`

```java
package com.teambind.profileserver.service.template;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.service.strategy.AttributeUpdateStrategy;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AttributeUpdateTemplate {

    /**
     * 속성 업데이트 템플릿 메서드
     *
     * @param userInfo 사용자 정보
     * @param desiredIds 원하는 속성 ID 목록
     * @param strategy 속성별 구체적인 전략
     */
    public void updateAttributes(
        UserInfo userInfo,
        List<Integer> desiredIds,
        AttributeUpdateStrategy strategy) {

        if (desiredIds == null) {
            return;  // null이면 변경 없음
        }

        String userId = userInfo.getUserId();
        Set<Integer> desired = new HashSet<>(desiredIds);
        List<Integer> currentList = strategy.findCurrentIds(userId);
        Set<Integer> current = new HashSet<>(currentList);

        // 삭제할 항목 계산
        Set<Integer> toRemove = new HashSet<>(current);
        toRemove.removeAll(desired);

        // 추가할 항목 계산
        Set<Integer> toAdd = new HashSet<>(desired);
        toAdd.removeAll(current);

        // 삭제 실행
        if (!toRemove.isEmpty()) {
            strategy.deleteByIds(userId, toRemove);
        } else if (desired.isEmpty() && !current.isEmpty()) {
            // 전체 삭제
            strategy.deleteByIds(userId, current);
        }

        // 추가 실행
        if (!toAdd.isEmpty()) {
            strategy.addAttributes(userInfo, toAdd);
        }
    }
}
```

**Step 3: 전략 구현체 작성**

`src/main/java/com/teambind/profileserver/service/strategy/GenreUpdateStrategy.java`

```java
package com.teambind.profileserver.service.strategy;

import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.key.UserGenreKey;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.UserGenresRepository;
import com.teambind.profileserver.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GenreUpdateStrategy implements AttributeUpdateStrategy<UserGenres> {
	
	private final UserGenresRepository userGenresRepository;
	private final GenreNameTableRepository genreNameTableRepository;
	private final UserInfoRepository userInfoRepository;
	
	@Override
	public List<Integer> findCurrentIds(String userId) {
		return userGenresRepository.findGenreIdsByUserId(userId);
	}
	
	@Override
	public void deleteByIds(String userId, Set<Integer> idsToRemove) {
		userGenresRepository.deleteByUserIdAndGenreIdsIn(userId, idsToRemove);
	}
	
	@Override
	public void addAttributes(UserInfo userInfo, Set<Integer> idsToAdd) {
		List<UserGenres> newGenres = idsToAdd.stream()
				.map(genreId -> UserGenres.builder()
						.id(new UserGenreKey(userInfo.getUserId(), genreId))
						.genre(genreNameTableRepository.getReferenceById(genreId))
						.build())
				.toList();
		
		newGenres.forEach(userInfo::addGenre);  // 편의 메서드 사용
	}
}
```

`src/main/java/com/teambind/profileserver/service/strategy/InstrumentUpdateStrategy.java`

```java
package com.teambind.profileserver.service.strategy;

import com.teambind.profileserver.entity.attribute.UserInstruments;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.key.UserInstrumentKey;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.repository.UserInstrumentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InstrumentUpdateStrategy implements AttributeUpdateStrategy<UserInstruments> {
	
	private final UserInstrumentsRepository userInstrumentsRepository;
	private final InstrumentNameTableRepository instrumentNameTableRepository;
	private final UserInfoRepository userInfoRepository;
	
	@Override
	public List<Integer> findCurrentIds(String userId) {
		return userInstrumentsRepository.findInstrumentIdsByUserId(userId);
	}
	
	@Override
	public void deleteByIds(String userId, Set<Integer> idsToRemove) {
		userInstrumentsRepository.deleteByUserIdAndInstrumentIdsIn(userId, idsToRemove);
	}
	
	@Override
	public void addAttributes(UserInfo userInfo, Set<Integer> idsToAdd) {
		List<UserInstruments> newInstruments = idsToAdd.stream()
				.map(instrumentId -> UserInstruments.builder()
						.id(new UserInstrumentKey(userInfo.getUserId(), instrumentId))
						.instrument(instrumentNameTableRepository.getReferenceById(instrumentId))
						.build())
				.toList();
		
		newInstruments.forEach(userInfo::addInstrument);  // 편의 메서드 사용
	}
}
```

**Step 4: Service에서 템플릿 사용**

```java
// ProfileUpdateService.java
@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final UserInfoRepository userInfoRepository;
    private final AttributeUpdateTemplate updateTemplate;
    private final GenreUpdateStrategy genreStrategy;
    private final InstrumentUpdateStrategy instrumentStrategy;

    @Transactional
    public UserInfo updateProfile(ProfileUpdateCommand command) {
        UserInfo userInfo = userInfoRepository.findById(command.getUserId())
            .orElseThrow(() -> new ProfileException(ErrorCode.USER_NOT_FOUND));

        updateBasicInfo(userInfo, command);

        // ✅ 템플릿 메서드로 간소화
        updateTemplate.updateAttributes(userInfo, command.getGenreIds(), genreStrategy);
        updateTemplate.updateAttributes(userInfo, command.getInstrumentIds(), instrumentStrategy);

        return userInfo;
    }

    // ... 나머지 메서드 ...
}
```

#### 기대 효과
- ✅ 코드 중복 약 70줄 제거
- ✅ 새로운 속성(예: 선호 지역) 추가 시 전략만 구현하면 됨
- ✅ 단일 책임 원칙 준수
- ✅ 테스트 용이성 향상

#### 작업 체크리스트
- [ ] AttributeUpdateStrategy 인터페이스 정의
- [ ] AttributeUpdateTemplate 구현
- [ ] GenreUpdateStrategy 구현
- [ ] InstrumentUpdateStrategy 구현
- [ ] ProfileUpdateService 리팩토링
- [ ] 전략 패턴 단위 테스트 작성
- [ ] 통합 테스트 통과 확인

---

## Phase 4: 아키텍처 개선

**예상 기간**: 3-4주
**우선순위**: 🟡 중요

### 4.1 도메인 중심 패키지 구조로 전환

#### 목표
기술 중심에서 도메인 중심으로 패키지 구조 개편

#### 현재 구조 (기술 중심)
```
com.teambind.profileserver
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

#### 개선안 (도메인 중심)
```
com.teambind.profileserver
├── domain/
│   ├── profile/                    # 프로필 도메인
│   │   ├── entity/
│   │   │   ├── UserInfo.java
│   │   │   ├── UserGenres.java
│   │   │   ├── UserInstruments.java
│   │   │   └── History.java
│   │   ├── repository/
│   │   │   ├── UserInfoRepository.java
│   │   │   ├── UserGenresRepository.java
│   │   │   └── UserInstrumentsRepository.java
│   │   ├── service/
│   │   │   ├── ProfileUpdateService.java
│   │   │   ├── ProfileSearchService.java
│   │   │   └── ProfileCreateService.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── controller/
│   │       ├── ProfileUpdateController.java
│   │       └── ProfileSearchController.java
│   │
│   ├── attribute/                  # 속성 도메인 (장르, 악기 등)
│   │   ├── entity/
│   │   │   ├── GenreNameTable.java
│   │   │   ├── InstrumentNameTable.java
│   │   │   └── LocationNameTable.java
│   │   ├── repository/
│   │   └── service/
│   │
│   └── validation/                 # 검증 도메인
│       ├── annotation/
│       └── validator/
│
├── infrastructure/                 # 인프라 계층
│   ├── persistence/
│   │   ├── querydsl/
│   │   │   └── ProfileSearchRepositoryImpl.java
│   │   └── config/
│   │       └── QuerydslConfig.java
│   ├── kafka/
│   │   ├── consumer/
│   │   └── event/
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ErrorCode.java
│       └── ProfileException.java
│
└── application/                    # 어플리케이션 계층
    ├── mapper/
    ├── facade/                     # 여러 도메인을 조합하는 경우
    └── config/
```

#### 마이그레이션 계획

**Phase 4.1.1: 패키지 구조 생성 및 파일 이동 (1주)**
- [ ] 새로운 패키지 구조 생성
- [ ] 파일 이동 (Git history 유지를 위해 `git mv` 사용)
- [ ] import 문 자동 수정 (IDE 기능 활용)

**Phase 4.1.2: 의존성 정리 (1주)**
- [ ] 도메인 간 의존성 확인
- [ ] 순환 참조 제거
- [ ] 인터페이스 분리 (필요 시)

**Phase 4.1.3: 테스트 수정 및 검증 (1주)**
- [ ] 테스트 코드 패키지 구조 동기화
- [ ] 모든 테스트 통과 확인
- [ ] 통합 테스트 추가 실행

#### 기대 효과
- ✅ 도메인 로직 파악 용이
- ✅ 응집도 향상, 결합도 감소
- ✅ 신규 개발자 온보딩 시간 단축
- ✅ 도메인별 독립적인 개발/배포 가능 (향후 모듈화 대비)

### 4.2 AOP 기반 성능 모니터링

#### 목표
실행 시간을 자동으로 측정하고 느린 메서드를 감지

#### 구현안

`src/main/java/com/teambind/profileserver/infrastructure/aop/PerformanceLoggingAspect.java`

```java
package com.teambind.profileserver.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    private static final long SLOW_THRESHOLD_MS = 1000;  // 1초

    @Around("execution(* com.teambind.profileserver.domain..service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > SLOW_THRESHOLD_MS) {
            log.warn("⚠️ Slow execution detected: {} took {}ms",
                     joinPoint.getSignature().toShortString(), executionTime);
        } else {
            log.debug("✅ {} executed in {}ms",
                      joinPoint.getSignature().toShortString(), executionTime);
        }

        return result;
    }

    @Around("execution(* com.teambind.profileserver.domain..repository..*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > 500) {  // Repository는 500ms 기준
            log.warn("⚠️ Slow query detected: {} took {}ms",
                     joinPoint.getSignature().toShortString(), executionTime);
        }

        return result;
    }
}
```

#### 추가 도구 통합

**build.gradle 추가**
```gradle
dependencies {
    // 성능 모니터링
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    // 쿼리 로깅
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
}
```

**application.yml**
```yaml
# P6Spy 쿼리 로깅
decorator:
  datasource:
    p6spy:
      enable-logging: true
      multiline: true
      logging: slf4j

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

---

## Phase 5: 신규 기능 추가

**예상 기간**: 4주+
**우선순위**: 🟢 개선

### 5.1 캐싱 전략 도입

#### 목표
읽기 빈도가 높은 데이터에 캐싱을 적용하여 성능 향상

#### 캐싱 대상

1. **Genre/Instrument 네임 테이블** (거의 변경되지 않음)
2. **공개 프로필 조회** (읽기 빈도 높음)
3. **검색 결과** (동일 조건 반복 조회 가능성)

#### 구현안

**Step 1: Redis 의존성 추가**

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
}
```

**Step 2: 캐시 설정**

`src/main/java/com/teambind/profileserver/infrastructure/config/CacheConfig.java`

```java
package com.teambind.profileserver.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // 기본 TTL 10분
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Genre/Instrument - 1시간 (거의 변경 없음)
        cacheConfigurations.put("genres", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("instruments", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 공개 프로필 - 5분
        cacheConfigurations.put("publicProfiles", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 검색 결과 - 1분
        cacheConfigurations.put("searchResults", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

**Step 3: Service에 캐싱 적용**

```java
// GenreService.java
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreNameTableRepository genreRepository;

    @Cacheable(value = "genres", key = "#genreId")
    public GenreNameTable getGenreById(int genreId) {
        return genreRepository.findById(genreId)
            .orElseThrow(() -> new ProfileException(ErrorCode.GENRE_NOT_FOUND));
    }

    @Cacheable(value = "genres", key = "'all'")
    public List<GenreNameTable> getAllGenres() {
        return genreRepository.findAll();
    }

    @CacheEvict(value = "genres", allEntries = true)
    public void clearGenreCache() {
        // 관리자가 장르를 추가/수정할 때 호출
    }
}

// ProfileSearchService.java
@Service
@RequiredArgsConstructor
public class ProfileSearchService {

    @Cacheable(value = "publicProfiles", key = "#userId")
    public UserResponse getPublicProfile(String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId)
            .orElseThrow(() -> new ProfileException(ErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(userInfo.getIsPublic())) {
            throw new ProfileException(ErrorCode.PROFILE_NOT_PUBLIC);
        }

        return profileMapper.toResponse(userInfo);
    }

    @CacheEvict(value = "publicProfiles", key = "#userId")
    public void evictProfileCache(String userId) {
        // 프로필 업데이트 시 호출
    }
}

// ProfileUpdateService.java
@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final ProfileSearchService profileSearchService;

    @Transactional
    public UserInfo updateProfile(ProfileUpdateCommand command) {
        UserInfo updated = // ... 업데이트 로직 ...

        // 캐시 무효화
        profileSearchService.evictProfileCache(command.getUserId());

        return updated;
    }
}
```

#### 기대 효과
- ✅ Genre/Instrument 조회 시간 95% 감소
- ✅ 공개 프로필 조회 응답 시간 50-70% 단축
- ✅ DB 부하 감소
- ✅ 동시 사용자 처리 능력 향상

### 5.2 API 문서화 자동화 (SpringDoc)

#### 목표
Swagger UI를 통한 API 문서 자동 생성 및 테스트 환경 제공

#### 구현안

**Step 1: 의존성 추가**

```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}
```

**Step 2: OpenAPI 설정**

```java
package com.teambind.profileserver.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI profileServerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Profile Server API")
                .version("v1.0.0")
                .description("TeamBind 프로필 관리 및 검색 API")
                .contact(new Contact()
                    .name("TeamBind Team")
                    .email("contact@teambind.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.teambind.com").description("Production")
            ));
    }
}
```

**Step 3: Controller에 문서화 어노테이션 추가**

```java
@RestController
@RequestMapping("/api/profiles/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileUpdateController {

    @Operation(
        summary = "프로필 부분 업데이트",
        description = "사용자 프로필의 일부 필드를 업데이트합니다. null인 필드는 변경하지 않습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 성공",
                     content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{userId}/ver1")
    public ResponseEntity<UserResponse> updateProfile(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        @PathVariable String userId,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "업데이트할 프로필 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ProfileUpdateRequest.class),
                examples = @ExampleObject(
                    name = "example",
                    value = "{\n" +
                            "  \"nickname\": \"newNickname\",\n" +
                            "  \"city\": \"Seoul\",\n" +
                            "  \"introduction\": \"Hello!\",\n" +
                            "  \"chattable\": true,\n" +
                            "  \"publicProfile\": true,\n" +
                            "  \"sex\": \"M\",\n" +
                            "  \"genres\": {\"1\": \"Rock\", \"2\": \"Jazz\"},\n" +
                            "  \"instruments\": {\"1\": \"Guitar\", \"2\": \"Piano\"}\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody ProfileUpdateRequest request) {

        // ...
    }
}
```

#### 접근 URL
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

#### 기대 효과
- ✅ API 문서 자동 생성 (수동 작성 불필요)
- ✅ 프론트엔드 팀과 협업 효율 향상
- ✅ API 테스트 환경 제공
- ✅ API 스펙 변경 시 문서 자동 업데이트

---

## 구현 우선순위 매트릭스

### 긴급도 vs 중요도 매트릭스

```
        중요도 높음                    중요도 낮음
긴급도   ┌──────────────────────┬──────────────────────┐
높음     │ 🔴 Phase 1 & 2      │ 🟡 Quick Wins        │
         │ - 상속 구조          │ - 네이밍 수정         │
         │ - 연관관계 편의 메서드  │ - 로깅 개선           │
         │ - Bean Validation    │                      │
         ├──────────────────────┼──────────────────────┤
낮음     │ 🟡 Phase 3 & 4      │ 🟢 Phase 5           │
         │ - Mapper 패턴        │ - 캐싱               │
         │ - Service 리팩토링    │ - API 문서화          │
         │ - 패키지 구조 개선     │ - 모니터링            │
         └──────────────────────┴──────────────────────┘
```

### ROI (투자 대비 효과) 순위

| 순위 | 항목 | 투자 시간 | 효과 | ROI |
|------|------|----------|------|-----|
| 1 | 연관관계 편의 메서드 추가 | 0.5주 | ⭐⭐⭐⭐⭐ | 매우 높음 |
| 2 | Bean Validation 도입 | 1주 | ⭐⭐⭐⭐⭐ | 매우 높음 |
| 3 | 상속 구조 적용 | 1주 | ⭐⭐⭐⭐ | 높음 |
| 4 | 캐싱 도입 (Genre/Instrument) | 0.5주 | ⭐⭐⭐⭐ | 높음 |
| 5 | Mapper 패턴 도입 | 1주 | ⭐⭐⭐⭐ | 높음 |
| 6 | Service 리팩토링 (Template Method) | 1.5주 | ⭐⭐⭐⭐ | 중간 |
| 7 | API 문서화 (SpringDoc) | 0.5주 | ⭐⭐⭐ | 중간 |
| 8 | 패키지 구조 개편 | 2주 | ⭐⭐⭐ | 중간 |
| 9 | AOP 모니터링 | 1주 | ⭐⭐⭐ | 중간 |

---

## 예상 일정 및 리소스

### 전체 타임라인 (총 8-10주)

```
Week 1-2:  Phase 1 - 객체지향 설계 개선
  ├─ Week 1: 상속 구조 적용
  └─ Week 2: 연관관계 편의 메서드

Week 3-4:  Phase 2 - 검증 구조 현대화
  ├─ Week 3: Bean Validation 구현
  └─ Week 4: 테스트 및 문서화

Week 5-7:  Phase 3 - 코드 품질 개선
  ├─ Week 5: Mapper 패턴 도입
  ├─ Week 6: Service 리팩토링
  └─ Week 7: 코드 리뷰 및 테스트

Week 8-10: Phase 4 & 5 - 아키텍처 개선 및 신규 기능
  ├─ Week 8: 패키지 구조 개편
  ├─ Week 9: 캐싱 및 모니터링
  └─ Week 10: API 문서화 및 최종 테스트
```

### 필요 리소스

#### 인력
- **백엔드 개발자**: 1-2명 (Full-time)
- **QA 엔지니어**: 1명 (Part-time, Week 4, 7, 10)
- **DevOps**: 1명 (Part-time, Redis 설정 시)

#### 환경
- **개발 환경**: 기존 로컬 환경
- **테스트 환경**: Docker Compose (Redis 추가)
- **모니터링**: Prometheus + Grafana (선택)

### 체크포인트

#### Week 2 종료 시
- [ ] UserGenres/UserInstruments 상속 구조 완료
- [ ] 연관관계 편의 메서드 추가 완료
- [ ] 기존 테스트 모두 통과
- [ ] 코드 리뷰 완료

#### Week 4 종료 시
- [ ] Bean Validation 전면 적용
- [ ] 검증 관련 테스트 커버리지 90% 이상
- [ ] API 응답 에러 메시지 개선 확인

#### Week 7 종료 시
- [ ] ProfileUpdateService 메서드 평균 길이 20줄 이하
- [ ] 코드 중복률 5% 이하
- [ ] Mapper 패턴 전면 적용

#### Week 10 종료 시 (최종)
- [ ] 전체 테스트 커버리지 80% 이상
- [ ] Swagger UI 문서화 완료
- [ ] 성능 저하 없음 (오히려 개선)
- [ ] 프로덕션 배포 준비 완료

---

## 위험 관리

### 잠재적 위험 요소

| 위험 | 확률 | 영향도 | 대응 방안 |
|------|------|--------|----------|
| 대규모 리팩토링으로 인한 버그 발생 | 중간 | 높음 | 단계별 배포, 충분한 테스트, 롤백 계획 수립 |
| 캐싱 도입 시 데이터 불일치 | 낮음 | 중간 | TTL 짧게 설정, 캐시 무효화 전략 명확히 |
| 패키지 구조 변경 시 Git 이력 손실 | 중간 | 낮음 | `git mv` 사용, 변경 전 브랜치 백업 |
| 일정 지연 | 중간 | 중간 | 우선순위 명확화, 주간 진행도 체크 |

---

## 다음 단계

### 즉시 착수 가능한 작업 (Quick Wins)

1. **네이밍 수정** (2시간)
   - `UserGenres.userId` → `id`
   - `UserInstruments.userId` → `id`
   - `ProfileUpdateValidator.NicknameValidation` → `isValidNickname`

2. **로깅 개선** (1시간)
   - 주요 Service 메서드에 `log.debug()` 추가
   - 에러 발생 시 스택 트레이스 로깅

3. **DTO Getter 정리** (30분)
   - `@Data`와 `@Getter` 중복 제거
   - Lombok 어노테이션 정리

### 첫 주 실행 계획

**Day 1-2: 환경 설정**
- Git 브랜치 생성 (`feature/oop-improvements`)
- TODO 리스트 작성
- 팀원과 계획 공유

**Day 3-5: 상속 구조 구현**
- `UserAttributeBase` 추상 클래스 생성
- `UserGenres` 리팩토링
- `UserInstruments` 리팩토링
- 단위 테스트 작성

**Day 6-7: 연관관계 편의 메서드**
- `UserInfo`에 편의 메서드 추가
- `ProfileUpdateService` 일부 리팩토링
- 통합 테스트 실행

### 최종 목표

**3개월 후**:
- ✅ SOLID 원칙에 부합하는 깔끔한 코드
- ✅ Bean Validation 기반 선언적 검증
- ✅ 코드 중복 최소화 (DRY 원칙 준수)
- ✅ 도메인 중심 아키텍처
- ✅ 자동화된 API 문서
- ✅ 견고한 테스트 기반

**비전**:
> "누구나 쉽게 이해하고 수정할 수 있는, 확장 가능한 프로필 서버"

---

## 참고 자료

### 추천 도서
- **Effective Java (3rd Edition)** - Joshua Bloch
- **클린 코드** - Robert C. Martin
- **리팩토링 (2판)** - Martin Fowler
- **도메인 주도 설계 철저 입문** - 나루세 마사노부

### 추천 학습 자료
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/reference/)
- [Bean Validation Specification](https://beanvalidation.org/)
- [Hibernate User Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)
- [Refactoring Guru - Design Patterns](https://refactoring.guru/design-patterns)

### 유용한 도구
- **IntelliJ IDEA Plugins**:
  - SonarLint (코드 품질 분석)
  - JPA Buddy (JPA 엔티티 관리)
  - Rainbow Brackets (가독성 향상)

- **분석 도구**:
  - SonarQube (정적 분석)
  - JaCoCo (테스트 커버리지)
  - P6Spy (쿼리 로깅)

---
