# 데이터베이스 설정 가이드

## 목차

1. [빠른 시작](#빠른-시작)
2. [수동 설정](#수동-설정)
3. [마이그레이션](#마이그레이션)
4. [SQL 파일 설명](#sql-파일-설명)
5. [문제 해결](#문제-해결)

## 빠른 시작

### 1. Docker Compose로 MariaDB 시작

```bash
# 전체 인프라 시작
docker-compose up -d

# MariaDB만 시작
docker-compose up -d mariadb
```

### 2. 데이터베이스 초기화

```bash
# 자동 초기화 스크립트 실행
./db-init.sh
```

이 명령어가 자동으로 수행하는 작업:

- ✅ 데이터베이스 생성
- ✅ 테이블 스키마 생성 (8개 테이블)
- ✅ 인덱스 생성 (15개 인덱스)
- ✅ 초기 데이터 삽입 (지역 18개, 장르 23개, 악기 14개)
- ✅ 연결 테스트

### 3. Spring Boot 애플리케이션 실행

```bash
./gradlew bootRun
```

## 🔧 수동 설정

### 1. 데이터베이스 생성

```sql
CREATE DATABASE profiles
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 2. 스키마 생성

```bash
mysql -h localhost -P 4000 -u root -p profiles < src/main/resources/sql/schema.sql
```

### 3. 초기 데이터 삽입

```bash
mysql -h localhost -P 4000 -u root -p profiles < src/main/resources/sql/Data.sql
```

### 4. 테이블 확인

```sql
USE profiles;
SHOW TABLES;

-- 예상 결과:
-- genre_name
-- instrument_name
-- location_names
-- profile_update_history
-- shedlock
-- user_genres
-- user_info
-- user_instruments
```

## 마이그레이션

기존 데이터베이스가 있는 경우:

### 1. 백업

```bash
mysqldump -u root -p profiles > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 마이그레이션 실행

```bash
mysql -h localhost -P 4000 -u root -p profiles < src/main/resources/sql/migration.sql
```

### 3. 검증

```sql
-- 테이블 구조 확인
DESCRIBE user_info;

-- 데이터 개수 확인
SELECT 'location_names' as table_name, COUNT(*) FROM location_names
UNION ALL SELECT 'genre_name', COUNT(*) FROM genre_name
UNION ALL SELECT 'instrument_name', COUNT(*) FROM instrument_name;

-- 인덱스 확인
SHOW INDEX FROM user_info;
```

## SQL 파일 설명

### schema.sql

**목적**: 전체 데이터베이스 스키마 정의

**포함 내용**:

- 8개 테이블 정의
	- `user_info`: 사용자 프로필 정보
	- `location_names`: 지역 마스터
	- `genre_name`: 장르 마스터
	- `instrument_name`: 악기 마스터
	- `user_genres`: 사용자-장르 매핑
	- `user_instruments`: 사용자-악기 매핑
	- `profile_update_history`: 변경 이력
	- `shedlock`: 스케줄러 분산 락

- 15개 인덱스 정의
	- 단일 컬럼 인덱스 (10개)
	- 복합 인덱스 (5개)

**특징**:

- UTF-8 MB4 인코딩 (이모지 지원)
- InnoDB 스토리지 엔진
- 외래키 제약조건
- ON DELETE CASCADE 설정
- 낙관적 락 (version 컬럼)

### Data.sql

**목적**: 초기 마스터 데이터 삽입

**포함 데이터**:

- 지역 (18개): 서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 기타
- 장르 (23개): ROCK, POP, JAZZ, CLASSICAL, HIP_HOP, ELECTRONIC, 등
- 악기 (14개): VOCAL, GUITAR, BASS, DRUM, KEYBOARD, 등

### migration.sql

**목적**: 기존 DB → 새 스키마 마이그레이션

**주요 작업**:

1. `user_info` 테이블 구조 변경
	- `introduction` 컬럼 추가
	- `city` 컬럼 타입 변경 (ENUM → VARCHAR)
	- `profile_image_url` 길이 확장

2. `location_names` 테이블 생성

3. 기존 데이터 변환
	- 한글 지역명 → 영문 코드 변환

4. 인덱스 생성

5. 롤백 스크립트 포함

## 데이터베이스 구조

### ERD (Entity Relationship Diagram)

```
┌─────────────────┐
│  location_names │
│  (지역 마스터)   │
└─────────────────┘
         ↓ (참조)
┌─────────────────┐         ┌──────────────────┐
│   user_info     │←────────│ user_genres      │
│  (사용자 정보)   │         │ (사용자-장르 M:N) │
└─────────────────┘         └──────────────────┘
         ↓                           ↓
         ↓                  ┌──────────────────┐
         ↓                  │  genre_name      │
         ↓                  │  (장르 마스터)    │
         ↓                  └──────────────────┘
         ↓
         ↓                  ┌──────────────────┐
         └──────────────────│ user_instruments │
                            │ (사용자-악기 M:N) │
                            └──────────────────┘
                                     ↓
                            ┌──────────────────┐
                            │ instrument_name  │
                            │  (악기 마스터)    │
                            └──────────────────┘
```

### 주요 제약조건

1. **Unique 제약**
	- `user_info.nickname`: 닉네임 중복 불가

2. **Foreign Key 제약**
	- `user_genres.user_id` → `user_info.user_id`
	- `user_genres.genre_id` → `genre_name.genre_id`
	- `user_instruments.user_id` → `user_info.user_id`
	- `user_instruments.instrument_id` → `instrument_name.instrument_id`
	- `profile_update_history.user_id` → `user_info.user_id`

3. **Cascade 동작**
	- 사용자 삭제 시 관련 장르/악기/이력 자동 삭제

## 인덱스 전략

### 1. 검색 성능 최적화

```sql
-- 지역별 검색
idx_user_info_city

-- 복합 조건 검색 (지역+성별+공개여부)
idx_user_info_composite_search

-- 최근 가입자 순
idx_user_info_created_at
```

### 2. JOIN 성능 최적화

```sql
-- 장르 역방향 조회 (특정 장르를 좋아하는 사용자)
idx_user_genres_genre_id

-- 악기 역방향 조회 (특정 악기를 다루는 사용자)
idx_user_instruments_instrument_id
```

### 3. 이력 조회 최적화

```sql
-- 사용자별 이력
idx_history_user_id

-- 최근 이력
idx_history_updated_at

-- 사용자별 최근 이력 (복합)
idx_history_composite
```

자세한 내용은 [DATABASE_OPTIMIZATION.md](./DATABASE_OPTIMIZATION.md) 참조

## 문제 해결

### 1. 연결 실패

```bash
# MariaDB 컨테이너 상태 확인
docker ps | grep mariadb

# 로그 확인
docker logs mariadb

# 재시작
docker-compose restart mariadb
```

### 2. 권한 문제

```sql
-- root 권한 확인
SHOW GRANTS FOR 'root'@'%';

-- 필요시 권한 부여
GRANT ALL PRIVILEGES ON profiles.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

### 3. 테이블이 이미 존재

```sql
-- 모든 테이블 삭제 (주의!)
DROP DATABASE IF EXISTS profiles;
CREATE DATABASE profiles;

-- 또는 개별 테이블 삭제
DROP TABLE IF EXISTS user_instruments;
DROP TABLE IF EXISTS user_genres;
DROP TABLE IF EXISTS profile_update_history;
DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS instrument_name;
DROP TABLE IF EXISTS genre_name;
DROP TABLE IF EXISTS location_names;
DROP TABLE IF EXISTS shedlock;
```

### 4. 인덱스 재생성

```sql
-- 인덱스 확인
SHOW INDEX FROM user_info;

-- 인덱스 삭제
DROP INDEX idx_user_info_city ON user_info;

-- 인덱스 재생성
CREATE INDEX idx_user_info_city ON user_info(city);
```

### 5. 성능 문제

```sql
-- 테이블 통계 업데이트
ANALYZE TABLE user_info;

-- 인덱스 통계 확인
SELECT * FROM information_schema.statistics
WHERE table_schema = 'profiles';

-- 쿼리 실행 계획 확인
EXPLAIN SELECT * FROM user_info WHERE city = 'SEOUL';
```

## 추가 리소스

- [DATABASE_OPTIMIZATION.md](./DATABASE_OPTIMIZATION.md) - 성능 최적화 가이드
- [DOCKER_SETUP.md](../DOCKER_SETUP.md) - Docker 환경 설정
- [MariaDB Documentation](https://mariadb.com/kb/en/)
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)

## 보안 권장사항

1. **프로덕션 환경**
	- `.env` 파일을 `.gitignore`에 추가
	- 강력한 비밀번호 사용
	- 외부 접근 제한 (방화벽 설정)

2. **백업**
	- 정기적인 백업 스케줄 설정
	- 원격 저장소에 백업 파일 보관

3. **모니터링**
	- Slow Query Log 활성화
	- 인덱스 사용률 모니터링
	- 디스크 공간 모니터링
