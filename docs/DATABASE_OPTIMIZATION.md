# Database 최적화 가이드

## 인덱스 전략

### 1. user_info 테이블

#### 생성된 인덱스

```sql
-- 단일 컬럼 인덱스
CREATE INDEX idx_user_info_nickname ON user_info(nickname);
CREATE INDEX idx_user_info_city ON user_info(city);
CREATE INDEX idx_user_info_created_at ON user_info(created_at DESC);
CREATE INDEX idx_user_info_is_public ON user_info(is_public);

-- 복합 인덱스
CREATE INDEX idx_user_info_composite_search ON user_info(city, sex, is_public);
```

#### 최적화된 쿼리 패턴

** 인덱스 활용 (GOOD)**

```sql
-- 1. 지역별 검색 (idx_user_info_city 사용)
SELECT * FROM user_info WHERE city = 'SEOUL';

-- 2. 복합 조건 검색 (idx_user_info_composite_search 사용)
SELECT * FROM user_info
WHERE city = 'SEOUL'
  AND sex = 'M'
  AND is_public = TRUE;

-- 3. 최근 가입 순 (idx_user_info_created_at 사용)
SELECT * FROM user_info
ORDER BY created_at DESC
LIMIT 10;

-- 4. 닉네임 검색 (idx_user_info_nickname 사용)
SELECT * FROM user_info WHERE nickname LIKE 'john%';
```

** Full Table Scan (BAD)**

```sql
-- 1. 인덱스 순서 무시
SELECT * FROM user_info
WHERE sex = 'M'           -- 복합 인덱스의 첫 컬럼(city)이 없음
  AND city = 'SEOUL';

-- 2. 함수 사용
SELECT * FROM user_info WHERE UPPER(nickname) = 'JOHN';

-- 3. LIKE 중간 매칭
SELECT * FROM user_info WHERE nickname LIKE '%john%';
```

### 2. user_genres / user_instruments 테이블

#### 생성된 인덱스

```sql
-- user_genres
CREATE INDEX idx_user_genres_genre_id ON user_genres(genre_id);

-- user_instruments
CREATE INDEX idx_user_instruments_instrument_id ON user_instruments(instrument_id);
```

#### 최적화된 쿼리 패턴

** 인덱스 활용 (GOOD)**

```sql
-- 1. 특정 장르를 좋아하는 사용자 검색
SELECT u.*
FROM user_info u
INNER JOIN user_genres ug ON u.user_id = ug.user_id
WHERE ug.genre_id = 1;  -- idx_user_genres_genre_id 사용

-- 2. 여러 장르 검색 (IN 사용)
SELECT DISTINCT u.*
FROM user_info u
INNER JOIN user_genres ug ON u.user_id = ug.user_id
WHERE ug.genre_id IN (1, 2, 3);

-- 3. 특정 악기를 다루는 사용자
SELECT u.*
FROM user_info u
INNER JOIN user_instruments ui ON u.user_id = ui.user_id
WHERE ui.instrument_id = 2;  -- idx_user_instruments_instrument_id 사용
```

### 3. profile_update_history 테이블

#### 생성된 인덱스

```sql
CREATE INDEX idx_history_user_id ON profile_update_history(user_id);
CREATE INDEX idx_history_updated_at ON profile_update_history(updated_at DESC);
CREATE INDEX idx_history_field_name ON profile_update_history(field_name);
CREATE INDEX idx_history_composite ON profile_update_history(user_id, updated_at DESC);
```

#### 최적화된 쿼리 패턴

** 인덱스 활용 (GOOD)**

```sql
-- 1. 사용자별 최근 이력 조회 (idx_history_composite 사용)
SELECT * FROM profile_update_history
WHERE user_id = 'user123'
ORDER BY updated_at DESC
LIMIT 10;

-- 2. 특정 필드 변경 이력 (idx_history_field_name 사용)
SELECT * FROM profile_update_history
WHERE field_name = 'nickname'
ORDER BY updated_at DESC;

-- 3. 최근 전체 이력 (idx_history_updated_at 사용)
SELECT * FROM profile_update_history
ORDER BY updated_at DESC
LIMIT 100;
```

## 실행 계획 분석

### EXPLAIN 사용법

```sql
-- 인덱스 사용 여부 확인
EXPLAIN SELECT * FROM user_info WHERE city = 'SEOUL';

-- 실제 실행 계획 확인
EXPLAIN ANALYZE SELECT * FROM user_info WHERE city = 'SEOUL';
```

### 좋은 실행 계획 지표

- `type`: `ref`, `range`, `index` (✅ GOOD)
- `type`: `ALL` ( BAD - Full Table Scan)
- `rows`: 검색 행 수가 적을수록 좋음
- `Extra`: `Using index` (✅ Covering Index)
- `Extra`: `Using filesort`, `Using temporary` (⚠️ 주의)

## 성능 모니터링 쿼리

### 1. 인덱스 사용률 확인

```sql
-- 테이블별 인덱스 사용 통계
SELECT
    table_name,
    index_name,
    cardinality,
    seq_in_index
FROM information_schema.statistics
WHERE table_schema = 'profiles'
ORDER BY table_name, seq_in_index;
```

### 2. 느린 쿼리 확인

```sql
-- slow query log 활성화
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 1초 이상 쿼리 로깅

-- 느린 쿼리 확인
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

### 3. 인덱스 사이즈 확인

```sql
SELECT
    table_name,
    index_name,
    ROUND(stat_value * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
FROM mysql.innodb_index_stats
WHERE database_name = 'profiles'
  AND stat_name = 'size'
ORDER BY stat_value DESC;
```

## 쿼리 최적화 팁

### 1. 복합 인덱스 활용 규칙

**인덱스 순서가 중요합니다!**

```sql
-- 복합 인덱스: (city, sex, is_public)

--  GOOD: 인덱스 순서대로 사용
WHERE city = 'SEOUL' AND sex = 'M' AND is_public = TRUE
WHERE city = 'SEOUL' AND sex = 'M'
WHERE city = 'SEOUL'

--  BAD: 첫 번째 컬럼(city)이 없음
WHERE sex = 'M' AND is_public = TRUE
```

### 2. JOIN 최적화

```sql
--  GOOD: INNER JOIN + 작은 테이블 먼저
SELECT u.*, g.genre_name
FROM user_genres ug
INNER JOIN user_info u ON ug.user_id = u.user_id
INNER JOIN genre_name g ON ug.genre_id = g.genre_id
WHERE ug.genre_id = 1;

--  BAD: 큰 테이블을 먼저 스캔
SELECT u.*, g.genre_name
FROM user_info u
LEFT JOIN user_genres ug ON u.user_id = ug.user_id
LEFT JOIN genre_name g ON ug.genre_id = g.genre_id
WHERE ug.genre_id = 1;
```

### 3. 페이징 최적화

```sql
--  BAD: OFFSET이 크면 느림
SELECT * FROM user_info
ORDER BY created_at DESC
LIMIT 10000, 10;

--  GOOD: 커서 기반 페이징
SELECT * FROM user_info
WHERE created_at < '2024-01-01 00:00:00'
ORDER BY created_at DESC
LIMIT 10;
```

### 4. COUNT(*) 최적화

```sql
--  BAD: 전체 테이블 스캔
SELECT COUNT(*) FROM user_info;

--  GOOD: 인덱스 사용
SELECT COUNT(*) FROM user_info WHERE city = 'SEOUL';

--  BETTER: 근사치 사용 (대용량 테이블)
SELECT table_rows
FROM information_schema.tables
WHERE table_schema = 'profiles'
  AND table_name = 'user_info';
```

## 인덱스 유지보수

### 1. 인덱스 통계 업데이트

```sql
-- 테이블별 통계 분석
ANALYZE TABLE user_info;
ANALYZE TABLE user_genres;
ANALYZE TABLE user_instruments;
ANALYZE TABLE profile_update_history;
```

### 2. 사용하지 않는 인덱스 확인

```sql
SELECT
    object_schema,
    object_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE index_name IS NOT NULL
  AND count_star = 0
  AND object_schema = 'profiles'
ORDER BY object_schema, object_name;
```

### 3. 중복 인덱스 확인

```sql
SELECT
    table_name,
    GROUP_CONCAT(index_name) as duplicate_indexes
FROM information_schema.statistics
WHERE table_schema = 'profiles'
GROUP BY table_name, column_name
HAVING COUNT(*) > 1;
```

## 캐싱 전략

### 1. QueryDSL 결과 캐싱

```java
@Cacheable(value = "userProfile", key = "#userId")
public UserInfo getUserProfile(String userId) {
    return profileSearchRepository.search(userId);
}
```

### 2. Redis 캐싱 대상

- 마스터 데이터 (genre_name, instrument_name, location_names)
- 자주 조회되는 사용자 프로필
- 검색 결과 (지역/장르별)

### 3. 캐시 무효화 전략

```java
@CacheEvict(value = "userProfile", key = "#userId")
public void updateProfile(String userId, ProfileUpdateRequest request) {
    // 업데이트 로직
}
```

## 성능 목표

| 항목        | 목표      | 측정 방법           |
|-----------|---------|-----------------|
| 단일 프로필 조회 | < 10ms  | EXPLAIN ANALYZE |
| 복합 조건 검색  | < 50ms  | EXPLAIN ANALYZE |
| 페이징 조회    | < 100ms | EXPLAIN ANALYZE |
| 프로필 업데이트  | < 20ms  | 애플리케이션 로그       |
| 이력 저장     | < 10ms  | 애플리케이션 로그       |

## 체크리스트

- [ ] 모든 WHERE 절 컬럼에 인덱스 생성
- [ ] 복합 인덱스 순서 최적화
- [ ] JOIN 쿼리 실행 계획 확인
- [ ] 페이징 쿼리 커서 기반으로 변경
- [ ] 마스터 데이터 Redis 캐싱
- [ ] Slow Query Log 모니터링
- [ ] 주기적 인덱스 통계 업데이트
- [ ] 사용하지 않는 인덱스 제거
