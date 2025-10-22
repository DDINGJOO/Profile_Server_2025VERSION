-- ====================================
-- Profile Server Database Schema
-- ====================================
-- 최종 업데이트: 2025-10-22
-- MariaDB 10.11 호환
-- 인덱스 최적화 포함

-- 1. 사용자 정보 테이블
CREATE TABLE IF NOT EXISTS user_info (
    user_id VARCHAR(255) PRIMARY KEY COMMENT '사용자 고유 ID (외부 인증 서버에서 제공)',
    profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL',
    sex CHAR(1) COMMENT '성별 (M/F/O)',
    nickname VARCHAR(100) NOT NULL UNIQUE COMMENT '닉네임 (중복 불가)',
    city VARCHAR(100) COMMENT '지역 (LocationNameTable의 city_id 참조)',
    introduction TEXT COMMENT '자기소개',
    version INT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일',
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정일',
    is_public BOOLEAN DEFAULT TRUE COMMENT '프로필 공개 여부',
    is_chatable BOOLEAN DEFAULT TRUE COMMENT '채팅 가능 여부'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 지역 명칭 테이블
CREATE TABLE IF NOT EXISTS location_names (
    city_id VARCHAR(50) PRIMARY KEY COMMENT '지역 코드 (예: SEOUL, BUSAN)',
    city_name VARCHAR(100) NOT NULL COMMENT '지역 한글명 (예: 서울, 부산)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 장르 명칭 테이블
CREATE TABLE IF NOT EXISTS genre_name (
    genre_id INT PRIMARY KEY COMMENT '장르 고유 ID',
    genre_name VARCHAR(100) NOT NULL COMMENT '장르명 (예: ROCK, JAZZ)',
    version INT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 악기 명칭 테이블
CREATE TABLE IF NOT EXISTS instrument_name (
    instrument_id INT PRIMARY KEY COMMENT '악기 고유 ID',
    instrument_name VARCHAR(100) NOT NULL COMMENT '악기명 (예: GUITAR, DRUM)',
    version INT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 사용자-장르 매핑 테이블
CREATE TABLE IF NOT EXISTS user_genres (
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID',
    genre_id INT NOT NULL COMMENT '장르 ID',
    version INT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    PRIMARY KEY (user_id, genre_id),
    CONSTRAINT fk_user_genres_user FOREIGN KEY (user_id)
        REFERENCES user_info(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_genres_genre FOREIGN KEY (genre_id)
        REFERENCES genre_name(genre_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 사용자-악기 매핑 테이블
CREATE TABLE IF NOT EXISTS user_instruments (
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID',
    instrument_id INT NOT NULL COMMENT '악기 ID',
    version INT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    PRIMARY KEY (user_id, instrument_id),
    CONSTRAINT fk_instruments_user FOREIGN KEY (user_id)
        REFERENCES user_info(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_instruments_instrument FOREIGN KEY (instrument_id)
        REFERENCES instrument_name(instrument_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 프로필 변경 이력 테이블
CREATE TABLE IF NOT EXISTS profile_update_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 고유 ID',
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID',
    field_name VARCHAR(100) NOT NULL COMMENT '변경된 필드명',
    old_val TEXT COMMENT '변경 전 값',
    new_val TEXT COMMENT '변경 후 값',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변경 시각',
    CONSTRAINT fk_history_user FOREIGN KEY (user_id)
        REFERENCES user_info(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. ShedLock 테이블 (스케줄러 분산 락)
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '락 이름',
    lock_until TIMESTAMP(3) NOT NULL COMMENT '락 만료 시간',
    locked_at TIMESTAMP(3) NOT NULL COMMENT '락 획득 시간',
    locked_by VARCHAR(255) NOT NULL COMMENT '락 소유자'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================
-- 인덱스 생성 (성능 최적화)
-- ====================================

-- user_info 테이블 인덱스
CREATE INDEX idx_user_info_nickname ON user_info(nickname);
CREATE INDEX idx_user_info_city ON user_info(city);
CREATE INDEX idx_user_info_created_at ON user_info(created_at DESC);
CREATE INDEX idx_user_info_is_public ON user_info(is_public);
CREATE INDEX idx_user_info_composite_search ON user_info(city, sex, is_public);

-- user_genres 테이블 인덱스
CREATE INDEX idx_user_genres_genre_id ON user_genres(genre_id);

-- user_instruments 테이블 인덱스
CREATE INDEX idx_user_instruments_instrument_id ON user_instruments(instrument_id);

-- profile_update_history 테이블 인덱스
CREATE INDEX idx_history_user_id ON profile_update_history(user_id);
CREATE INDEX idx_history_updated_at ON profile_update_history(updated_at DESC);
CREATE INDEX idx_history_field_name ON profile_update_history(field_name);
CREATE INDEX idx_history_composite ON profile_update_history(user_id, updated_at DESC);

-- location_names 테이블 인덱스
CREATE INDEX idx_location_city_name ON location_names(city_name);

-- genre_name 테이블 인덱스
CREATE INDEX idx_genre_name ON genre_name(genre_name);

-- instrument_name 테이블 인덱스
CREATE INDEX idx_instrument_name ON instrument_name(instrument_name);

-- ====================================
-- 주석 및 설명
-- ====================================

-- 인덱스 전략:
-- 1. 단일 컬럼 인덱스: WHERE 절에서 자주 사용되는 컬럼
-- 2. 복합 인덱스: 다중 조건 검색이 자주 발생하는 경우 (카디널리티 높은 순서)
-- 3. 정렬 인덱스: ORDER BY에 사용되는 컬럼 (DESC 명시)
-- 4. FK 인덱스: JOIN 성능 향상

-- 쿼리 최적화 포인트:
-- - user_info: 지역/성별/공개여부로 검색 시 복합 인덱스 활용
-- - profile_update_history: 사용자별 이력 조회 시 복합 인덱스 활용
-- - user_genres/user_instruments: 역방향 조회(장르→사용자) 성능 향상
