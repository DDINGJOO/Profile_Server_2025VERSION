-- ====================================
-- Profile Server Initial Data
-- ====================================
-- 최종 업데이트: 2025-10-22
-- 장르, 악기, 지역 초기 데이터

-- ====================================
-- 1. 지역 데이터
-- ====================================
INSERT INTO location_names (city_id, city_name) VALUES ('SEOUL', '서울');
INSERT INTO location_names (city_id, city_name) VALUES ('BUSAN', '부산');
INSERT INTO location_names (city_id, city_name) VALUES ('DAEGU', '대구');
INSERT INTO location_names (city_id, city_name) VALUES ('INCHEON', '인천');
INSERT INTO location_names (city_id, city_name) VALUES ('GWANGJU', '광주');
INSERT INTO location_names (city_id, city_name) VALUES ('DAEJEON', '대전');
INSERT INTO location_names (city_id, city_name) VALUES ('ULSAN', '울산');
INSERT INTO location_names (city_id, city_name) VALUES ('SEJONG', '세종');
INSERT INTO location_names (city_id, city_name) VALUES ('GYEONGGI', '경기');
INSERT INTO location_names (city_id, city_name) VALUES ('GANGWON', '강원');
INSERT INTO location_names (city_id, city_name) VALUES ('CHUNGBUK', '충북');
INSERT INTO location_names (city_id, city_name) VALUES ('CHUNGNAM', '충남');
INSERT INTO location_names (city_id, city_name) VALUES ('JEONBUK', '전북');
INSERT INTO location_names (city_id, city_name) VALUES ('JEONNAM', '전남');
INSERT INTO location_names (city_id, city_name) VALUES ('GYEONGBUK', '경북');
INSERT INTO location_names (city_id, city_name) VALUES ('GYEONGNAM', '경남');
INSERT INTO location_names (city_id, city_name) VALUES ('JEJU', '제주');
INSERT INTO location_names (city_id, city_name) VALUES ('ETC', '기타');

-- ====================================
-- 2. 장르 데이터 (23개)
-- ====================================
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (1,  'ROCK', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (2,  'POP', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (3,  'JAZZ', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (4,  'CLASSICAL', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (5,  'HIP_HOP', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (6,  'ELECTRONIC', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (7,  'FOLK', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (8,  'BLUES', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (9,  'REGGAE', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (10, 'METAL', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (11, 'COUNTRY', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (12, 'LATIN', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (13, 'RNB', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (14, 'SOUL', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (15, 'FUNK', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (16, 'PUNK', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (17, 'ALTERNATIVE', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (18, 'INDIE', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (19, 'GOSPEL', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (20, 'OPERA', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (21, 'SOUNDTRACK', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (22, 'WORLD_MUSIC', 0);
INSERT INTO genre_name (genre_id, genre_name, version) VALUES (23, 'OTHER', 0);

-- ====================================
-- 3. 악기 데이터 (14개)
-- ====================================
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (1,  'VOCAL', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (2,  'GUITAR', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (3,  'BASS', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (4,  'DRUM', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (5,  'KEYBOARD', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (6,  'PERCUSSION', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (7,  'SAXOPHONE', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (8,  'VIOLIN', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (9,  'CELLO', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (10, 'TRUMPET', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (11, 'FLUTE', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (12, 'DJ', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (13, 'PRODUCER', 0);
INSERT INTO instrument_name (instrument_id, instrument_name, version) VALUES (14, 'ETC', 0);

-- ====================================
-- 데이터 설명
-- ====================================

-- 지역 (18개)
-- - 특별시/광역시: 서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종
-- - 도: 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주
-- - 기타: 해외 거주 또는 미선택

-- 장르 (23개)
-- - 대중음악: ROCK, POP, HIP_HOP, ELECTRONIC, RNB, SOUL, FUNK
-- - 전통/클래식: CLASSICAL, JAZZ, BLUES, FOLK, GOSPEL, OPERA
-- - 하위장르: METAL, PUNK, ALTERNATIVE, INDIE, REGGAE, COUNTRY, LATIN
-- - 기타: SOUNDTRACK, WORLD_MUSIC, OTHER

-- 악기 (14개)
-- - 현악기: GUITAR, BASS, VIOLIN, CELLO
-- - 관악기: SAXOPHONE, TRUMPET, FLUTE
-- - 타악기: DRUM, PERCUSSION
-- - 건반: KEYBOARD
-- - 보컬: VOCAL
-- - 기타: DJ, PRODUCER, ETC
