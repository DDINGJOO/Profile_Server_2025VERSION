# Profile Server 심층 분석 보고서

> 작성일: 2025-10-20
> 대상: Profile Server 리팩토링 및 설계 개선

---

## 📑 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [객체지향 설계 분석](#객체지향-설계-분석)
3. [검증(Validation) 구조 분석](#검증validation-구조-분석)
4. [아키텍처 패턴 분석](#아키텍처-패턴-분석)
5. [코드 품질 분석](#코드-품질-분석)
6. [종합 평가](#종합-평가)

---

## 프로젝트 개요

### 기술 스택
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **ORM**: JPA + QueryDSL 5.0.0
- **Database**: MariaDB (Production), H2 (Test)
- **Message Queue**: Kafka
- **Build Tool**: Gradle

### 프로젝트 구조
```
src/main/java/com/teambind/profileserver/
├── controller/          # REST API 엔드포인트
├── service/            # 비즈니스 로직
│   ├── create/
│   ├── update/
│   ├── search/
│   └── history/
├── repository/         # 데이터 액세스
│   ├── dsl/           # QueryDSL 구현
│   └── search/        # 검색 관련
├── entity/            # JPA 엔티티
│   ├── nameTable/     # 코드 테이블
│   └── key/           # 복합키
├── dto/               # 데이터 전송 객체
├── utils/             # 유틸리티
│   ├── validator/     # 검증 로직
│   └── generator/     # 생성 로직
├── exceptions/        # 예외 처리
└── events/           # 이벤트 처리
```

---

## 객체지향 설계 분석

### 1. SOLID 원칙 준수 여부

#### ✅ 잘 지켜진 원칙

**단일 책임 원칙 (SRP) - 부분적 준수**
- `ProfileUpdateService`, `ProfileSearchService` 등 서비스가 역할별로 분리됨
- Validator들이 각자의 검증 책임만 수행 (`NickNameValidator`, `GenreValidator` 등)

**인터페이스 분리 원칙 (ISP) - 양호**
- Repository 인터페이스들이 적절히 분리됨
- `ProfileUpdateValidator` 인터페이스가 검증 관련 메서드만 정의

**의존성 역전 원칙 (DIP) - 양호**
- 서비스 계층이 Repository 인터페이스에 의존
- Validator 인터페이스를 통한 추상화 활용

#### ❌ 개선이 필요한 부분

**단일 책임 원칙 (SRP) 위반 사례**

```java
// ProfileUpdateService.java:42-146
public UserInfo updateProfile(String userId, String nickname,
    List<Integer> instruments, List<Integer> genres,
    boolean isChattable, boolean isPublicProfile,
    Character sex, String city, String introduction) {

    // 1. 사용자 조회
    // 2. 닉네임 검증 및 업데이트
    // 3. 악기 목록 업데이트 (추가/삭제 계산 포함)
    // 4. 장르 목록 업데이트 (추가/삭제 계산 포함)
    // 5. 히스토리 저장
    // 6. 엔티티 저장
}
```

**문제점**:
- 하나의 메서드가 너무 많은 책임을 가짐 (100줄 이상)
- 악기/장르 업데이트 로직이 중복 (거의 동일한 코드 반복)
- 비즈니스 로직과 영속성 관리가 혼재

**개방-폐쇄 원칙 (OCP) 위반 사례**

```java
// UserGenres.java와 UserInstruments.java
// 두 클래스가 거의 동일한 구조를 가지지만 상속 구조가 없음

@Entity
@Table(name = "user_genres")
public class UserGenres {
    @EmbeddedId
    private UserGenreKey userId;  // ❌ 네이밍도 혼란스러움

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    private GenreNameTable genre;

    @Version
    private int version;
}

@Entity
@Table(name = "user_instruments")
public class UserInstruments {
    @EmbeddedId
    private UserInstrumentKey userId;  // ❌ 동일한 패턴 반복

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instrumentId")
    private InstrumentNameTable instrument;

    @Version
    private int version;
}
```

**문제점**:
- 새로운 사용자 속성(예: 선호 지역, 활동 시간대 등)을 추가하려면 또 다시 동일한 클래스를 작성해야 함
- 변경에 닫혀있지 않음 (확장할 때마다 코드 중복)

### 2. 디자인 패턴 분석

#### ✅ 잘 활용된 패턴

**Repository 패턴**
```java
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    boolean existsByNickname(String nickname);
}
```
- 데이터 액세스 로직을 잘 캡슐화

**Strategy 패턴 (부분적)**
```java
public interface ProfileUpdateValidator {
    void validateProfileUpdateRequest(...);
    boolean NicknameValidation(String nickname);
    boolean isGenreValidByIds(Map<Integer,String> genreMap);
    void isInstrumentValidByIds(Map<Integer, String> instrumentMap);
}
```
- Validator를 인터페이스로 추상화하여 전략 교체 가능

**Code Table 패턴**
```java
@Entity
@Table(name = "genre_name_table")
public class GenreNameTable {
    @Id
    private int genreId;
    private String genreName;
}
```
- 서버 재시작 없이 장르/악기 추가 가능

#### ❌ 필요하지만 누락된 패턴

**Template Method 패턴 부재**

현재 `ProfileUpdateService`의 `updateProfile()` 메서드를 보면:

```java
// 악기 업데이트 로직 (63-96줄)
if (instruments != null) {
    Set<Integer> desiredInstruments = new HashSet<>(instruments);
    List<Integer> currentInstList = userInstrumentsRepository.findInstrumentIdsByUserId(userId);
    Set<Integer> currentInstruments = new HashSet<>(currentInstList);

    Set<Integer> toRemove = new HashSet<>(currentInstruments);
    toRemove.removeAll(desiredInstruments);

    Set<Integer> toAdd = new HashSet<>(desiredInstruments);
    toAdd.removeAll(currentInstruments);

    // 삭제 및 추가 로직...
}

// 장르 업데이트 로직 (99-131줄) - 거의 동일한 코드 반복!
if (genres != null) {
    Set<Integer> desiredGenres = new HashSet<>(genres);
    List<Integer> currentGenreList = userGenresRepository.findGenreIdsByUserId(userId);
    Set<Integer> currentGenres = new HashSet<>(currentGenreList);

    Set<Integer> toRemove = new HashSet<>(currentGenres);
    toRemove.removeAll(desiredGenres);

    Set<Integer> toAdd = new HashSet<>(desiredGenres);
    toAdd.removeAll(currentGenres);

    // 삭제 및 추가 로직...
}
```

**개선 제안**: 공통 알고리즘을 Template Method로 추출

**Mapper 패턴 부재**

현재는 DTO → Entity 변환이 Service 레이어에 산재:

```java
// ProfileUpdateController.java:23
profileUpdateService.updateProfile(
    userId,
    request.getNickname(),
    request.getInstruments().keySet().stream().toList(),
    request.getGenres().keySet().stream().toList(),
    request.isChattable(),
    request.isPublicProfile(),
    request.getSex(),
    request.getCity(),
    request.getIntroduction()
);
```

**문제점**:
- 파라미터가 9개로 과다 (Clean Code 원칙 위반)
- 변환 로직이 Controller에 노출됨
- 테스트하기 어려움

### 3. 연관관계 관리 분석

#### ❌ 연관관계 편의 메서드 부재

```java
// UserInfo.java:50-54
@OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.LAZY)
private List<UserGenres> userGenres;

@OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.LAZY)
private List<UserInstruments> userInstruments;
```

양방향 연관관계가 선언되어 있지만 편의 메서드가 없어, Service 계층에서 수동으로 양쪽 관계를 설정해야 함:

```java
// ProfileUpdateService.java:84-95
List<UserInstruments> uiBatch = new ArrayList<>(toAdd.size());
UserInfo userRef = userInfoRepository.getReferenceById(userId);
for (Integer instId : toAdd) {
    uiBatch.add(UserInstruments.builder()
            .userId(new UserInstrumentKey(userId, instId))
            .userInfo(userRef)  // ❌ 수동으로 양쪽 관계 설정
            .instrument(instrumentNameTableRepository.getReferenceById(instId))
            .build());
}
userInstrumentsRepository.saveAll(uiBatch);
```

**위험성**:
- 실수로 한쪽 관계만 설정하면 불일치 발생
- 비즈니스 로직이 복잡해짐
- 영속성 관리가 Service에 노출됨

### 4. 네이밍 분석

#### ❌ 혼란스러운 네이밍

**UserGenres.java:18**
```java
@EmbeddedId
private UserGenreKey userId;  // ❌ 타입은 UserGenreKey인데 변수명이 userId
```

**UserInstruments.java:22**
```java
@EmbeddedId
private UserInstrumentKey userId;  // ❌ 동일한 문제
```

**ProfileUpdateValidator.java:10**
```java
boolean NicknameValidation(String nickname);  // ❌ 메서드명이 PascalCase
```

**ProfileUpdateRequest.java**
```java
private boolean chattable;      // ❌ is 접두사 없음
private boolean publicProfile;  // ❌ is 접두사 없음
```

반면 Entity에서는:
```java
private Boolean isChatable;  // ✅ is 접두사 있음
private Boolean isPublic;    // ✅ is 접두사 있음
```

---

## 검증(Validation) 구조 분석

### 현재 구조

#### 1. 계층 구조
```
ProfileUpdateValidator (interface)
    ↓
ProfileUpdateValidatorImpl (implementation)
    ↓ (의존성 주입)
├── NickNameValidator
├── GenreValidator
└── InstrumentsValidator
```

#### 2. 검증 방식

**NickNameValidator.java:12-20**
```java
@Component
public class NickNameValidator {
    @Value("${nickname.validation.regex:^[a-zA-Z0-9_]{3,15}$}")
    private String regex;

    public boolean isValidNickName(String nickName) {
        if(!nickName.matches(regex)||nickName.isEmpty()){
            return false;
        };
        return true;
    }
}
```

**GenreValidator.java:19-25**
```java
public boolean isValidGenreByIds(Map<Integer, String> genre) {
    validateGenreSize(genre.size());  // 최대 3개 확인
    validateGenreIds(genre);          // ID-Name 매핑 확인
    return true;
}
```

**사용 예시 - ProfileUpdateController.java:22**
```java
@PutMapping("/{userId}/ver1")
public ResponseEntity<Boolean> updateProfile(
    @PathVariable String userId,
    @RequestBody ProfileUpdateRequest request) {

    // ❌ 수동으로 validator 호출
    profileUpdateValidator.validateProfileUpdateRequest(
        request.getNickname(),
        request.getInstruments(),
        request.getGenres()
    );

    profileUpdateService.updateProfile(...);
    return ResponseEntity.ok(true);
}
```

### ❌ 현재 방식의 문제점

#### 1. Bean Validation (JSR-380) 미활용

**ProfileUpdateRequest.java**
```java
@Data
public class ProfileUpdateRequest {
    private String nickname;           // ❌ 검증 어노테이션 없음
    private String city;
    private String introduction;
    private boolean chattable;
    private boolean publicProfile;
    private Character sex;
    private Map<Integer,String> genres;      // ❌ 검증 어노테이션 없음
    private Map<Integer,String> instruments; // ❌ 검증 어노테이션 없음
}
```

Spring에서 제공하는 표준 검증 방식을 사용하지 않아:
- 검증 로직이 분산됨 (Controller에서 수동 호출)
- 테스트하기 어려움
- 일관성 부족

#### 2. 검증 책임의 혼재

현재 검증이 세 곳에서 일어남:

1. **Validator 클래스** (utils.validator 패키지)
2. **Service 계층** (ProfileUpdateService.java:48-54)
   ```java
   if (nickname != null && !nickname.equals(userInfo.getNickname())) {
       if(userInfoRepository.existsByNickname(nickname)) {  // ❌ 비즈니스 검증
           throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
       }
   }
   ```
3. **Controller 계층** (수동 호출)

**문제점**:
- 어디서 무엇을 검증하는지 불명확
- 검증 누락 가능성
- 코드 중복

#### 3. 에러 메시지 관리 부실

**ErrorCode.java**
```java
NICKNAME_INVALID("PROFILE_008", "Nickname is invalid", HttpStatus.BAD_REQUEST),
GENRE_INVALID("PROFILE_009", "GenreId and Name are invalid", HttpStatus.BAD_REQUEST),
```

에러 메시지가 매우 일반적이어서:
- 사용자가 구체적으로 무엇이 잘못되었는지 알기 어려움
- "Nickname is invalid" → 길이가 문제인지, 형식이 문제인지, 중복인지 불명확

### ✅ 개선 방향: Bean Validation 기반 커스텀 어노테이션

#### 제안 1: 커스텀 어노테이션 생성

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameValidator.class)
public @interface ValidNickname {
    String message() default "닉네임은 3-15자의 영문, 숫자, 언더스코어만 가능합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenresValidator.class)
public @interface ValidGenres {
    String message() default "장르는 최대 3개까지 선택 가능합니다";
    int max() default 3;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### 제안 2: DTO에 적용

```java
@Data
public class ProfileUpdateRequest {
    @ValidNickname
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    @Size(max = 100, message = "자기소개는 100자 이내로 작성해주세요")
    private String introduction;

    @ValidGenres(max = 3)
    private Map<Integer, String> genres;

    @ValidInstruments(max = 3)
    private Map<Integer, String> instruments;
}
```

#### 제안 3: Controller에서 @Valid 사용

```java
@PutMapping("/{userId}/ver1")
public ResponseEntity<Boolean> updateProfile(
    @PathVariable String userId,
    @Valid @RequestBody ProfileUpdateRequest request) {  // ✅ @Valid 추가

    // 검증은 자동으로 실행됨 (수동 호출 불필요)
    profileUpdateService.updateProfile(userId, request);
    return ResponseEntity.ok(true);
}
```

#### 제안 4: 비즈니스 검증 분리

```java
// 형식 검증 (Format Validation) → Bean Validation
@ValidNickname  // 정규식 검증

// 비즈니스 검증 (Business Validation) → Service 계층
if (userInfoRepository.existsByNickname(nickname)) {
    throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
}
```

---

## 아키텍처 패턴 분석

### 1. 계층 구조

현재 프로젝트는 전통적인 3-Layer Architecture를 따름:

```
Controller (Presentation Layer)
    ↓
Service (Business Layer)
    ↓
Repository (Data Access Layer)
    ↓
Entity (Domain Model)
```

#### ✅ 강점

1. **명확한 책임 분리**
   - Controller: HTTP 요청/응답 처리
   - Service: 비즈니스 로직
   - Repository: 데이터 액세스

2. **DTO 사용**
   - Entity와 DTO를 분리하여 계층 간 결합도 감소
   - `ProfileUpdateRequest`, `UserResponse` 등

#### ❌ 개선점

1. **Service 계층 비대화**
   - `ProfileUpdateService`가 250줄로 너무 큼
   - 악기/장르 업데이트 로직이 중복

2. **DTO ↔ Entity 변환 로직 산재**
   - Mapper가 없어 변환 로직이 여기저기 흩어짐
   - Controller나 Service에서 직접 변환

### 2. 패키지 구조 분석

```
com.teambind.profileserver
├── controller/
├── service/
│   ├── create/
│   ├── update/
│   ├── search/
│   └── history/
├── repository/
│   ├── dsl/            ❌ 비일관적
│   └── search/         ❌ 비일관적
├── entity/
│   ├── nameTable/
│   └── key/
├── utils/
│   ├── validator/
│   └── generator/
├── dto/
│   ├── request/
│   └── response/
└── exceptions/
```

#### ❌ 문제점

1. **기술 중심 패키지 구조**
   - 현재: controller, service, repository로 구분
   - 문제: 프로필 관련 기능을 찾으려면 여러 패키지를 넘나들어야 함

2. **비일관적 하위 패키지**
   - repository 아래 `dsl/`과 `search/`가 혼재
   - service는 기능별(create/update/search), repository는 기술별(dsl)

3. **도메인 로직 파악 어려움**
   - 프로필의 핵심 기능이 무엇인지 패키지 구조만으로 알기 어려움

### 3. 의존성 분석

#### 현재 의존성 흐름

```
ProfileUpdateController
    ↓ (의존)
    ├─→ ProfileUpdateService
    │       ↓ (의존)
    │       ├─→ UserInfoRepository
    │       ├─→ UserGenresRepository
    │       ├─→ UserInstrumentsRepository
    │       ├─→ GenreNameTableRepository
    │       ├─→ InstrumentNameTableRepository
    │       └─→ UserProfileHistoryService
    │
    └─→ ProfileUpdateValidator
            ↓ (의존)
            ├─→ NickNameValidator
            ├─→ GenreValidator
            └─→ InstrumentsValidator
```

#### ✅ 장점
- DIP(의존성 역전 원칙) 준수: 인터페이스에 의존
- 순환 의존성 없음

#### ❌ 문제점
- `ProfileUpdateService`가 5개의 Repository에 의존 (과도한 의존성)
- Repository 계층이 너무 세분화되어 관리 포인트 증가

---

## 코드 품질 분석

### 1. 코드 중복 (DRY 원칙 위반)

#### 심각도: 🔴 높음

**UserGenres.java vs UserInstruments.java**
- 두 클래스가 95% 동일한 구조
- `@EmbeddedId`, `@MapsId`, `@ManyToOne`, `@Version` 패턴 반복
- 약 40줄의 코드 중복

**ProfileUpdateService.java**
- 악기 업데이트 로직 (63-96줄)
- 장르 업데이트 로직 (99-131줄)
- 약 68줄의 거의 동일한 코드 반복

**updateProfile() vs updateProfileAll()**
- 두 메서드가 매우 유사한 로직 (일부 업데이트 vs 전체 교체의 차이만)
- 공통 로직 추출 가능

### 2. 메서드 길이

#### 심각도: 🟡 중간

**ProfileUpdateService.java**
- `updateProfile()`: 104줄 (권장: 20줄 이하)
- `updateProfileAll()`: 73줄 (권장: 20줄 이하)

**긴 메서드의 문제점**:
- 이해하기 어려움
- 테스트하기 어려움
- 재사용 불가능

### 3. 매직 넘버/문자열

#### 심각도: 🟢 낮음 (잘 관리됨)

✅ 대부분의 상수를 application.properties로 외부화:
```java
@Value("${nickname.validation.regex:^[a-zA-Z0-9_]{3,15}$}")
private String regex;

@Value("${genres.validation.max-size:3}")
private int maxSize;
```

### 4. 예외 처리

#### 심각도: 🟢 낮음 (양호)

✅ **강점**:
- 중앙 집중식 예외 처리 (`GlobalExceptionHandler`)
- 일관된 에러 응답 구조 (`ErrorResponse`)
- 도메인별 에러 코드 체계 (PROFILE_001~009)

❌ **개선점**:
- 에러 메시지가 너무 일반적 ("Nickname is invalid")
- 예외 타입이 `ProfileException` 하나로 통합 (세분화 필요)

### 5. 테스트 커버리지

#### 심각도: 🟡 중간

**현황**:
- 11개의 테스트 파일 존재
- 주로 Service 계층 테스트

**부족한 부분**:
- Controller 통합 테스트 부재
- 동시성 테스트 없음 (낙관적 락 검증)
- Edge case 테스트 부족

---

## 종합 평가

### 강점 요약

#### 1. 성능 최적화 (⭐⭐⭐⭐⭐)
- QueryDSL을 활용한 N+1 문제 해결
- 배치 페치 조인으로 카테시안 곱 회피
- 커서 기반 페이징 구현
- **평가**: 매우 우수한 성능 설계

#### 2. 동시성 제어 (⭐⭐⭐⭐⭐)
- `@Version`을 활용한 낙관적 락
- 프로필 업데이트 충돌 방지
- **평가**: 견고한 동시성 관리

#### 3. 데이터베이스 설계 (⭐⭐⭐⭐)
- 코드 테이블 패턴으로 확장성 확보
- History 엔티티로 변경 추적
- **평가**: 우수한 DB 설계

#### 4. 예외 처리 (⭐⭐⭐⭐)
- 중앙 집중식 예외 처리
- 일관된 에러 응답
- **평가**: 양호한 에러 핸들링

### 약점 요약

#### 1. 객체지향 설계 (⭐⭐)
- ❌ UserGenres/UserInstruments 중복
- ❌ 연관관계 편의 메서드 부재
- ❌ Service 계층 SRP 위반
- **평가**: 개선 필요

#### 2. 검증 구조 (⭐⭐)
- ❌ Bean Validation 미활용
- ❌ 검증 책임 혼재
- ❌ 수동 validator 호출
- **평가**: 현대적 방식으로 전환 필요

#### 3. 코드 품질 (⭐⭐⭐)
- ❌ 심각한 코드 중복
- ❌ 긴 메서드
- ❌ 네이밍 혼선
- **평가**: 리팩토링 필요

#### 4. 아키텍처 (⭐⭐⭐)
- ❌ 기술 중심 패키지 구조
- ❌ Mapper 패턴 부재
- ❌ Service 비대화
- **평가**: 구조 개선 필요

### 우선순위별 개선 과제

#### 🔴 긴급 (1-2주)
1. UserGenres/UserInstruments 상속 구조 적용
2. 연관관계 편의 메서드 추가
3. 네이밍 수정 (userId → id)

#### 🟡 중요 (2-4주)
4. Bean Validation 기반 커스텀 어노테이션 구현
5. Mapper 패턴 도입 (DTO ↔ Entity)
6. ProfileUpdateService 리팩토링 (메서드 분리)

#### 🟢 개선 (1-2개월)
7. 도메인 중심 패키지 구조로 전환
8. 통합 테스트 추가
9. API 문서화 자동화 (SpringDoc)

