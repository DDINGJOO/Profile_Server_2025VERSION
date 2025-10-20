# Profile Server 종합 분석 및 향후 개발 가이드

> 작성일: 2025-10-20
> 대상: Profile Server 리팩토링 및 기능 개발

---

## 📊 프로젝트 현황 요약

### 기술 스택
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Database**: MariaDB (Production), H2 (Test)
- **ORM**: JPA + QueryDSL 5.0.0
- **Message Queue**: Kafka
- **Deployment**: Docker Compose + Nginx (3대 스케일 아웃)

### 아키텍처 개요
- MSA 환경에서 프로필 관리를 담당하는 독립 서버
- Auth Server와 Kafka로 이벤트 기반 연동
- 3대 인스턴스로 구성된 로드밸런싱 환경

---

## ✅ 프로젝트 강점

### 1. 우수한 성능 최적화
- **N+1 문제 해결**: QueryDSL을 활용한 배치 페치 조인 구현
  - `ProfileSearchRepositoryImpl.java:144-161` - `batchInitializeCollections()` 메서드
  - 별도의 쿼리로 컬렉션을 초기화하여 카테시안 곱 회피

- **효율적인 페이징**: 커서 기반 페이징 구현
  - Offset 기반 대비 대용량 데이터 처리에 최적화
  - `ProfileSearchRepositoryImpl.java:72-96`

### 2. 견고한 데이터베이스 설계
- **동시성 제어**: `@Version`을 활용한 낙관적 락 구현
  - 프로필 업데이트 충돌 방지
  - `UserInfo.java:32-34`, `UserGenres.java:34-36`

- **변경 이력 관리**: History 엔티티로 프로필 변경 추적
  - 감사(Audit) 기능 구현
  - `History.java`

### 3. 확장 가능한 아키텍처
- **코드 테이블 패턴**: Genre, Instrument를 별도 테이블로 관리
  - 서버 재시작 없이 새로운 장르/악기 추가 가능
  - `GenreNameTable.java`, `InstrumentNameTable.java`

- **Bulk 연산 최적화**:
  - `ProfileUpdateService.java:84-95, 119-130` - 배치 저장
  - JPQL DELETE 쿼리 활용

### 4. 명확한 계층 구조
- Controller → Service → Repository 분리
- DTO를 통한 계층 간 데이터 전달
- Exception 중앙 처리 (`GlobalExceptionHandler`)

### 5. 실용적인 테스트 코드
- 11개의 테스트 파일 존재
- 서비스 계층 중심 테스트 구현

---

## ⚠️ 프로젝트 약점 및 개선 필요 사항

### 1. 엔티티 설계 문제

#### 🔴 높은 우선순위
**Genre와 Instrument 상속 구조 미적용 (TODO.md 1번)**
- **현재 문제**:
  - `UserGenres`와 `UserInstruments`가 거의 동일한 구조로 중복
  - 두 엔티티 모두 같은 패턴의 `@EmbeddedId`, `@MapsId`, `@ManyToOne` 사용

- **개선 방안**:
  ```java
  // 제안: MappedSuperclass를 활용한 공통 추상 클래스
  @MappedSuperclass
  public abstract class UserAttributeBase<T> {
      @Version
      private int version;

      @ManyToOne(fetch = FetchType.LAZY)
      @ToString.Exclude
      @EqualsAndHashCode.Exclude
      protected UserInfo userInfo;
  }
  ```

- **기대 효과**:
  - 코드 중복 제거 (DRY 원칙)
  - 유지보수성 향상
  - 새로운 속성(예: 선호 지역) 추가 시 일관성 유지

### 2. 연관관계 편의 메서드 부재 (TODO.md 2번)

#### 🔴 높은 우선순위
- **현재 문제**:
  - `UserInfo.java:50-54`에서 양방향 연관관계 선언되어 있으나
  - 연관관계 편의 메서드가 없어 영속성 관리가 서비스 계층에 산재
  - `ProfileUpdateService.java:84-95`에서 수동으로 양쪽 관계 설정

- **개선 방안**:
  ```java
  // UserInfo.java
  public void addGenre(UserGenres userGenre) {
      if (this.userGenres == null) {
          this.userGenres = new ArrayList<>();
      }
      this.userGenres.add(userGenre);
      userGenre.setUserInfo(this);
  }

  public void removeGenre(UserGenres userGenre) {
      this.userGenres.remove(userGenre);
      userGenre.setUserInfo(null);
  }
  ```

- **기대 효과**:
  - 양방향 연관관계의 일관성 보장
  - 비즈니스 로직 단순화
  - 실수로 인한 버그 방지

### 3. 네이밍 및 패키지 구조 (TODO.md 3번)

#### 🟡 중간 우선순위
**문제점**:
1. **네이밍 혼선**:
   - `UserGenres.userId` (실제로는 `UserGenreKey` 타입) - `UserGenres.java:18`
   - `UserInstruments.userId` (실제로는 `UserInstrumentKey` 타입) - `UserInstruments.java:22`
   - 변수명이 타입을 오해하게 만듦

2. **패키지 구조 비일관성**:
   - `repository/dsl/` vs `repository/search/`
   - `entity/nameTable/` vs `entity/key/`

**개선 방안**:
```java
// 변경 전
@EmbeddedId
private UserGenreKey userId;  // 혼란스러움

// 변경 후
@EmbeddedId
private UserGenreKey id;  // 명확함
```

**패키지 구조 제안**:
```
com.teambind.profileserver
├── domain
│   ├── user
│   │   ├── entity (UserInfo, History)
│   │   ├── repository
│   │   └── service
│   └── attribute
│       ├── entity (UserGenres, UserInstruments)
│       ├── repository
│       └── service
├── infrastructure
│   ├── querydsl
│   └── kafka
└── api
    ├── controller
    └── dto
```

### 4. JPQL 활용 부족 (TODO.md 4번)

#### 🟡 중간 우선순위
- **현재 상태**:
  - `UserGenresRepository.java:18-27`에서 일부 JPQL 사용
  - 대부분의 복잡한 쿼리는 QueryDSL에 의존

- **개선 방안**:
  - 단순 쿼리는 JPQL `@Query`로 처리 (가독성)
  - 동적 쿼리는 QueryDSL 유지 (유연성)
  - 통계/집계 쿼리에 JPQL 활용 증대

**예시**:
```java
// 추가 가능한 JPQL 예시
@Query("SELECT ui FROM UserInfo ui " +
       "WHERE ui.isPublic = true " +
       "AND ui.city = :city " +
       "ORDER BY ui.createdAt DESC")
List<UserInfo> findPublicProfilesByCity(@Param("city") String city);
```

### 5. 예외 처리 및 검증 개선

#### 🟢 낮은 우선순위
- **부족한 부분**:
  - 입력 검증이 Validator 클래스에만 의존
  - Bean Validation (JSR-380) 미활용
  - 커스텀 예외가 `ProfileException` 하나로 통합

**개선 방안**:
```java
// DTO에 Bean Validation 추가
public class ProfileUpdateRequest {
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    private String nickname;

    @Size(max = 3, message = "장르는 최대 3개까지 선택 가능합니다")
    private Map<Integer, String> genres;
}

// 컨트롤러에서 @Valid 사용
@PutMapping("/{userId}/ver1")
public ResponseEntity<Boolean> updateProfile(
    @PathVariable String userId,
    @Valid @RequestBody ProfileUpdateRequest request) {
    // ...
}
```

### 6. API 버전 관리 비일관성

#### 🟢 낮은 우선순위
- **현재 문제**:
  - `ProfileUpdateController.java:19, 27` - `/ver1`, `/ver2` URL에 버전 포함
  - 다른 API는 버전 정보 없음
  - 버전 전략이 불명확

**개선 방안**:
```java
// 옵션 1: Header 기반 버전 관리
@GetMapping(value = "/profiles/{userId}",
            headers = "X-API-Version=1")

// 옵션 2: URI 기반 일관된 버전 관리
@RequestMapping("/api/v1/profiles")

// 옵션 3: Content Negotiation
@GetMapping(value = "/profiles/{userId}",
            produces = "application/vnd.teambind.v1+json")
```

### 7. 테스트 커버리지 확대 필요

#### 🟢 낮은 우선순위
- **현재 상태**: 11개 테스트 파일
- **부족한 부분**:
  - 통합 테스트 부재
  - Controller 레이어 테스트 부족
  - 동시성 테스트 없음 (낙관적 락 검증)

---

## 🚀 추가 개발 제안 사항

### 1. 성능 모니터링 및 로깅

#### 🔴 높은 우선순위
**현재 누락된 기능**:
- 쿼리 성능 모니터링
- 느린 쿼리 감지
- API 응답 시간 추적

**구현 제안**:
```java
// AOP를 활용한 성능 모니터링
@Aspect
@Component
public class PerformanceLoggingAspect {

    @Around("execution(* com.teambind.profileserver.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > 1000) { // 1초 이상
            log.warn("Slow execution: {} took {}ms",
                     joinPoint.getSignature(), executionTime);
        }
        return proceed;
    }
}
```

**추천 도구**:
- Spring Boot Actuator + Micrometer
- Prometheus + Grafana
- P6Spy (쿼리 로깅)

### 2. 캐싱 전략 도입

#### 🟡 중간 우선순위
**캐싱 대상**:
1. Genre/Instrument 네임 테이블 (거의 변경 없음)
2. 공개 프로필 조회 (읽기 빈도 높음)
3. 검색 필터 결과 (동일 조건 반복 조회)

**구현 제안**:
```java
// 로컬 캐시
@Cacheable(value = "genres", key = "#genreId")
public GenreNameTable getGenreById(int genreId) {
    return genreNameTableRepository.findById(genreId)
        .orElseThrow(() -> new ProfileException(ErrorCode.GENRE_NOT_FOUND));
}

// 분산 캐시 (Redis) - 스케일 아웃 환경에 적합
@Cacheable(value = "publicProfiles", key = "#userId")
public UserResponse getPublicProfile(String userId) {
    // ...
}
```

**캐시 무효화 전략**:
```java
@CacheEvict(value = "publicProfiles", key = "#userId")
public void updateProfile(String userId, ...) {
    // 프로필 업데이트 로직
}
```

### 3. API 문서화 자동화

#### 🟡 중간 우선순위
**SpringDoc (Swagger) 도입**:
```gradle
// build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

```java
// OpenAPI 설정
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI profileServerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Profile Server API")
                .version("v1.0")
                .description("프로필 관리 및 검색 API"));
    }
}

// Controller에 문서화
@Operation(summary = "프로필 조회", description = "사용자 ID로 프로필 조회")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공"),
    @ApiResponse(responseCode = "404", description = "사용자 없음")
})
@GetMapping("/{userId}")
public ResponseEntity<UserResponse> getProfiles(@PathVariable String userId) {
    // ...
}
```

### 4. 보안 강화

#### 🟡 중간 우선순위
**현재 누락**:
- 인증/인가 로직 (README에서 언급: "유저는 오직 개인의 프로필만 수정")
- Rate Limiting
- Input Sanitization

**구현 제안**:
```java
// Spring Security 통합
@PreAuthorize("hasRole('USER') and #userId == authentication.principal.username")
@PutMapping("/{userId}/ver1")
public ResponseEntity<Boolean> updateProfile(
    @PathVariable String userId,
    @RequestBody ProfileUpdateRequest request) {
    // ...
}

// Rate Limiting (Bucket4j)
@RateLimiter(name = "profileUpdate", fallbackMethod = "rateLimitFallback")
public ResponseEntity<Boolean> updateProfile(...) {
    // ...
}
```

### 5. 이벤트 기반 아키텍처 개선

#### 🟢 낮은 우선순위
**현재**: Kafka 의존성은 있으나 이벤트 처리 로직 미확인

**개선 제안**:
```java
// 도메인 이벤트 발행
@Service
public class ProfileUpdateService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateProfile(...) {
        // 업데이트 로직

        // 이벤트 발행
        eventPublisher.publishEvent(
            new ProfileUpdatedEvent(userId, nickname, genres, instruments)
        );
    }
}

// 이벤트 리스너 (Kafka Producer)
@Component
public class ProfileEventKafkaPublisher {

    @EventListener
    @Async
    public void handleProfileUpdated(ProfileUpdatedEvent event) {
        kafkaTemplate.send("profile-updates", event);
    }
}
```

### 6. 소프트 삭제(Soft Delete) 구현

#### 🟢 낮은 우선순위
**현재**: README에서 "탈퇴 후 3년 뒤 삭제" 언급

**구현 제안**:
```java
@Entity
@SQLDelete(sql = "UPDATE user_info SET deleted_at = NOW() WHERE user_id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserInfo {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

// 스케줄러로 3년 경과 데이터 물리 삭제
@Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
public void permanentlyDeleteOldProfiles() {
    LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3);
    userInfoRepository.deleteByDeletedAtBefore(threeYearsAgo);
}
```

### 7. 프로필 이미지 처리 연동

#### 🟢 낮은 우선순위
**README 언급**: "이미지 서버에 최신의 프로필 이미지를 받아와야 한다"

**구현 제안**:
```java
@Service
public class ProfileImageService {

    private final WebClient imageServerClient;

    public String getLatestProfileImageUrl(String userId) {
        return imageServerClient.get()
            .uri("/images/profile/{userId}/latest", userId)
            .retrieve()
            .bodyToMono(ImageUrlResponse.class)
            .map(ImageUrlResponse::getUrl)
            .block();
    }

    // 비동기 버전 (권장)
    public Mono<String> getLatestProfileImageUrlAsync(String userId) {
        return imageServerClient.get()
            .uri("/images/profile/{userId}/latest", userId)
            .retrieve()
            .bodyToMono(ImageUrlResponse.class)
            .map(ImageUrlResponse::getUrl);
    }
}
```

---

## 📋 우선순위별 실행 계획

### Phase 1: 긴급 리팩토링 (1-2주)
1. ✅ **연관관계 편의 메서드 추가**
   - `UserInfo`, `UserGenres`, `UserInstruments`에 편의 메서드 구현
   - 서비스 레이어 리팩토링

2. ✅ **네이밍 수정**
   - `UserGenres.userId` → `UserGenres.id`
   - `UserInstruments.userId` → `UserInstruments.id`
   - 전체 코드베이스 수정

3. ✅ **Genre/Instrument 상속 구조 적용**
   - `UserAttributeBase` 추상 클래스 생성
   - 중복 코드 제거

### Phase 2: 기능 개선 (2-3주)
1. ✅ **성능 모니터링 도입**
   - Spring Boot Actuator 설정
   - P6Spy 쿼리 로깅

2. ✅ **캐싱 전략 구현**
   - Genre/Instrument 로컬 캐시
   - Redis 분산 캐시 검토

3. ✅ **API 문서화**
   - SpringDoc 설정
   - 전체 API 문서화

### Phase 3: 아키텍처 개선 (3-4주)
1. ✅ **패키지 구조 재편**
   - 도메인 중심 패키지로 전환

2. ✅ **보안 강화**
   - Spring Security 통합
   - Rate Limiting

3. ✅ **테스트 확대**
   - 통합 테스트 추가
   - 동시성 테스트

### Phase 4: 고급 기능 (4주+)
1. ✅ **이벤트 아키텍처 개선**
2. ✅ **소프트 삭제 구현**
3. ✅ **이미지 서버 연동**

---

## 🔧 기술 부채 관리

### 즉시 해결 필요
- [ ] 연관관계 편의 메서드 부재
- [ ] 네이밍 혼선 (userId 변수명)

### 단기 해결 (1개월 내)
- [ ] Genre/Instrument 상속 구조
- [ ] JPQL 활용 확대
- [ ] 성능 모니터링

### 중장기 해결 (3개월 내)
- [ ] 패키지 구조 전환
- [ ] 보안 강화
- [ ] 테스트 커버리지 80% 달성

---

## 📈 성공 지표

### 성능
- [ ] API 응답 시간 평균 200ms 이하
- [ ] 검색 API 1000ms 이하 (복잡한 필터 포함)
- [ ] 동시 사용자 1000명 처리 가능

### 품질
- [ ] 테스트 커버리지 80% 이상
- [ ] Sonarqube 품질 게이트 통과
- [ ] 프로덕션 버그 월 5건 이하

### 개발 생산성
- [ ] 신규 기능 개발 시간 30% 단축
- [ ] 코드 리뷰 시간 50% 단축
- [ ] API 문서 자동 생성

---

## 🎯 결론

### 프로젝트의 현재 위치
이 프로젝트는 **성능 최적화**와 **확장 가능한 설계**에서 강점을 보이는 견고한 백엔드 시스템입니다. QueryDSL을 활용한 N+1 문제 해결, 커서 기반 페이징, 낙관적 락 등은 중급 이상의 설계 역량을 보여줍니다.

### 개선의 방향
TODO.md에 명시된 4가지 항목은 모두 **코드 품질**과 **유지보수성** 향상에 직결됩니다:
1. **상속 구조**: 코드 중복 제거로 유지보수성 향상
2. **연관관계 편의 메서드**: 버그 예방 및 비즈니스 로직 단순화
3. **네이밍 및 패키지 구조**: 가독성 및 협업 효율 향상
4. **JPQL 활용**: 단순 쿼리 가독성 개선

이 외에도 **캐싱**, **모니터링**, **보안** 등을 보완하면 프로덕션급 엔터프라이즈 시스템으로 발전할 수 있습니다.

### 투자 대비 효과가 높은 작업 Top 3
1. **연관관계 편의 메서드 추가** (1일 투자, 버그 감소 효과 큼)
2. **캐싱 도입** (2-3일 투자, 성능 향상 30-50%)
3. **API 문서화 자동화** (1일 투자, 협업 효율 대폭 향상)

---

## 참고 자료

### 추천 학습 자료
- [Effective Java 3rd Edition](https://www.oreilly.com/library/view/effective-java/9780134686097/) - 상속 설계, 네이밍
- [Spring Data JPA Best Practices](https://vladmihalcea.com/tutorials/hibernate/) - 연관관계 관리
- [High-Performance Java Persistence](https://vladmihalcea.com/books/high-performance-java-persistence/) - 캐싱, 쿼리 최적화

### 유용한 도구
- **IntelliJ IDEA Plugins**: SonarLint, JPA Buddy
- **성능 분석**: JProfiler, VisualVM
- **테스트**: JUnit 5, Testcontainers, JMeter

---

*이 문서는 프로젝트 현황 분석을 바탕으로 작성되었으며, 실제 개발 시 팀의 우선순위와 비즈니스 요구사항을 고려하여 조정해야 합니다.*
