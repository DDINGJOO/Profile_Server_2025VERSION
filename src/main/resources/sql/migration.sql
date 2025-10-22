-- ====================================
-- Database Migration Script
-- ====================================
-- 기존 DB에서 새 스키마로 마이그레이션
-- 주의: 프로덕션 환경에서는 반드시 백업 후 실행

-- ====================================
-- 1. 백업 권장 명령어
-- ====================================
-- mysqldump -u root -p profiles > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql

-- ====================================
-- 2. user_info 테이블 마이그레이션
-- ====================================

-- 2.1 introduction 컬럼 추가 (없는 경우)
ALTER TABLE user_info
ADD COLUMN IF NOT EXISTS introduction TEXT COMMENT '자기소개'
AFTER city;

-- 2.2 city 컬럼 타입 변경 (ENUM -> VARCHAR)
-- 기존 데이터 백업
CREATE TABLE IF NOT EXISTS user_info_city_backup AS
SELECT user_id, city FROM user_info;

-- city 컬럼 타입 변경
ALTER TABLE user_info
MODIFY COLUMN city VARCHAR(100) COMMENT '지역 (LocationNameTable의 city_id 참조)';

-- 2.3 profile_image_url 길이 확장
ALTER TABLE user_info
MODIFY COLUMN profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL';

-- 2.4 타임스탬프 기본값 설정
ALTER TABLE user_info
MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일';

ALTER TABLE user_info
MODIFY COLUMN last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정일';

-- ====================================
-- 3. location_names 테이블 생성
-- ====================================
CREATE TABLE IF NOT EXISTS location_names (
    city_id VARCHAR(50) PRIMARY KEY COMMENT '지역 코드 (예: SEOUL, BUSAN)',
    city_name VARCHAR(100) NOT NULL COMMENT '지역 한글명 (예: 서울, 부산)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 지역 데이터 삽입
INSERT IGNORE INTO location_names (city_id, city_name) VALUES
('SEOUL', '서울'),
('BUSAN', '부산'),
('DAEGU', '대구'),
('INCHEON', '인천'),
('GWANGJU', '광주'),
('DAEJEON', '대전'),
('ULSAN', '울산'),
('SEJONG', '세종'),
('GYEONGGI', '경기'),
('GANGWON', '강원'),
('CHUNGBUK', '충북'),
('CHUNGNAM', '충남'),
('JEONBUK', '전북'),
('JEONNAM', '전남'),
('GYEONGBUK', '경북'),
('GYEONGNAM', '경남'),
('JEJU', '제주'),
('ETC', '기타');

-- ====================================
-- 4. 기존 데이터 마이그레이션
-- ====================================

-- 4.1 user_info의 기존 city 데이터를 새 형식으로 변환
-- 예: '서울' -> 'SEOUL', '부산' -> 'BUSAN'
UPDATE user_info
SET city = CASE
    WHEN city = '서울' THEN 'SEOUL'
    WHEN city = '부산' THEN 'BUSAN'
    WHEN city = '대구' THEN 'DAEGU'
    WHEN city = '인천' THEN 'INCHEON'
    WHEN city = '광주' THEN 'GWANGJU'
    WHEN city = '대전' THEN 'DAEJEON'
    WHEN city = '울산' THEN 'ULSAN'
    WHEN city = '세종' THEN 'SEJONG'
    WHEN city = '경기' THEN 'GYEONGGI'
    WHEN city = '강원' THEN 'GANGWON'
    WHEN city = '충북' THEN 'CHUNGBUK'
    WHEN city = '충남' THEN 'CHUNGNAM'
    WHEN city = '전북' THEN 'JEONBUK'
    WHEN city = '전남' THEN 'JEONNAM'
    WHEN city = '경북' THEN 'GYEONGBUK'
    WHEN city = '경남' THEN 'GYEONGNAM'
    WHEN city = '제주' THEN 'JEJU'
    ELSE 'ETC'
END
WHERE city IS NOT NULL;

-- ====================================
-- 5. 인덱스 생성
-- ====================================

-- 5.1 user_info 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_user_info_city ON user_info(city);
CREATE INDEX IF NOT EXISTS idx_user_info_created_at ON user_info(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_info_is_public ON user_info(is_public);
CREATE INDEX IF NOT EXISTS idx_user_info_composite_search ON user_info(city, sex, is_public);

-- 5.2 user_genres 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_user_genres_genre_id ON user_genres(genre_id);

-- 5.3 user_instruments 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_user_instruments_instrument_id ON user_instruments(instrument_id);

-- 5.4 profile_update_history 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_history_user_id ON profile_update_history(user_id);
CREATE INDEX IF NOT EXISTS idx_history_updated_at ON profile_update_history(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_history_field_name ON profile_update_history(field_name);
CREATE INDEX IF NOT EXISTS idx_history_composite ON profile_update_history(user_id, updated_at DESC);

-- 5.5 마스터 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_location_city_name ON location_names(city_name);
CREATE INDEX IF NOT EXISTS idx_genre_name ON genre_name(genre_name);
CREATE INDEX IF NOT EXISTS idx_instrument_name ON instrument_name(instrument_name);

-- ====================================
-- 6. 마이그레이션 검증
-- ====================================

-- 6.1 테이블 구조 확인
DESCRIBE user_info;
DESCRIBE location_names;

-- 6.2 인덱스 확인
SHOW INDEX FROM user_info;
SHOW INDEX FROM user_genres;
SHOW INDEX FROM user_instruments;
SHOW INDEX FROM profile_update_history;

-- 6.3 데이터 개수 확인
SELECT 'user_info' as table_name, COUNT(*) as count FROM user_info
UNION ALL
SELECT 'location_names', COUNT(*) FROM location_names
UNION ALL
SELECT 'genre_name', COUNT(*) FROM genre_name
UNION ALL
SELECT 'instrument_name', COUNT(*) FROM instrument_name;

-- 6.4 city 데이터 마이그레이션 확인
SELECT
    city,
    COUNT(*) as count
FROM user_info
WHERE city IS NOT NULL
GROUP BY city;

-- ====================================
-- 7. 롤백 스크립트 (문제 발생 시)
-- ====================================

-- 7.1 city 데이터 복원
-- UPDATE user_info u
-- INNER JOIN user_info_city_backup b ON u.user_id = b.user_id
-- SET u.city = b.city;

-- 7.2 백업에서 전체 복원
-- mysql -u root -p profiles < backup_before_migration_YYYYMMDD_HHMMSS.sql

-- ====================================
-- 완료 메시지
-- ====================================
SELECT 'Migration completed successfully!' as status;
