-- ====================================
-- Sample Data for Testing
-- ====================================
-- 개발/테스트용 샘플 데이터
-- 주의: 프로덕션 환경에서는 실행하지 말 것!

-- ====================================
-- 1. 샘플 사용자 데이터 (10명)
-- ====================================

-- 1. 서울의 록 기타리스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user001', 'RockGuitarist', 'https://example.com/profile1.jpg', 'M', 'SEOUL',
        '안녕하세요! 록음악을 사랑하는 기타리스트입니다.', TRUE, TRUE, NOW() - INTERVAL 365 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user001', 1, 0);  -- ROCK
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user001', 2, 0);  -- GUITAR

-- 2. 부산의 재즈 피아니스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user002', 'JazzPianist', 'https://example.com/profile2.jpg', 'F', 'BUSAN',
        '재즈를 사랑하는 피아니스트입니다. 함께 연주해요!', TRUE, TRUE, NOW() - INTERVAL 300 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user002', 3, 0);  -- JAZZ
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user002', 5, 0);  -- KEYBOARD

-- 3. 경기의 힙합 프로듀서
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user003', 'HipHopProducer', 'https://example.com/profile3.jpg', 'M', 'GYEONGGI',
        '힙합 비트 만들어요. 콜라보 환영합니다!', TRUE, TRUE, NOW() - INTERVAL 250 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user003', 5, 0);  -- HIP_HOP
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user003', 13, 0);  -- PRODUCER

-- 4. 대구의 클래식 바이올리니스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user004', 'ClassicalViolinist', 'https://example.com/profile4.jpg', 'F', 'DAEGU',
        '클래식 음악을 전공하고 있습니다.', TRUE, TRUE, NOW() - INTERVAL 200 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user004', 4, 0);  -- CLASSICAL
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user004', 8, 0);  -- VIOLIN

-- 5. 인천의 팝 보컬리스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user005', 'PopSinger', 'https://example.com/profile5.jpg', 'F', 'INCHEON',
        'K-POP을 좋아하는 보컬리스트입니다. 듀엣 환영!', TRUE, TRUE, NOW() - INTERVAL 150 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user005', 2, 0);  -- POP
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user005', 1, 0);  -- VOCAL

-- 6. 광주의 일렉트로닉 DJ
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user006', 'ElectronicDJ', 'https://example.com/profile6.jpg', 'M', 'GWANGJU',
        'EDM 중심의 DJ입니다. 클럽 공연 경험 多', TRUE, TRUE, NOW() - INTERVAL 100 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user006', 6, 0);  -- ELECTRONIC
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user006', 12, 0);  -- DJ

-- 7. 대전의 인디 밴드 (기타+보컬)
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user007', 'IndieBandMember', 'https://example.com/profile7.jpg', 'M', 'DAEJEON',
        '인디 밴드에서 기타와 보컬을 담당합니다.', TRUE, TRUE, NOW() - INTERVAL 80 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user007', 18, 0);  -- INDIE
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user007', 2, 0);  -- GUITAR
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user007', 1, 0);  -- VOCAL

-- 8. 울산의 메탈 드러머
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user008', 'MetalDrummer', 'https://example.com/profile8.jpg', 'M', 'ULSAN',
        '메탈 밴드 드러머입니다. 강렬한 연주 좋아합니다!', TRUE, FALSE, NOW() - INTERVAL 60 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user008', 10, 0);  -- METAL
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user008', 4, 0);  -- DRUM

-- 9. 강원의 포크 어쿠스틱 기타리스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user009', 'FolkGuitarist', 'https://example.com/profile9.jpg', 'F', 'GANGWON',
        '어쿠스틱 기타로 포크송 연주합니다.', TRUE, TRUE, NOW() - INTERVAL 40 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user009', 7, 0);  -- FOLK
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user009', 2, 0);  -- GUITAR

-- 10. 제주의 R&B 베이시스트
INSERT INTO user_info (user_id, nickname, profile_image_url, sex, city, introduction, is_public, is_chatable, created_at)
VALUES ('user010', 'RnBBassist', 'https://example.com/profile10.jpg', 'M', 'JEJU',
        'R&B와 소울 음악의 그루브를 사랑합니다.', FALSE, TRUE, NOW() - INTERVAL 20 DAY);

INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user010', 13, 0);  -- RNB
INSERT INTO user_genres (user_id, genre_id, version) VALUES ('user010', 14, 0);  -- SOUL
INSERT INTO user_instruments (user_id, instrument_id, version) VALUES ('user010', 3, 0);  -- BASS

-- ====================================
-- 2. 샘플 프로필 변경 이력
-- ====================================

-- user001의 닉네임 변경 이력
INSERT INTO profile_update_history (user_id, field_name, old_val, new_val, updated_at)
VALUES ('user001', 'nickname', 'OldRockGuitarist', 'RockGuitarist', NOW() - INTERVAL 30 DAY);

-- user002의 도시 변경 이력
INSERT INTO profile_update_history (user_id, field_name, old_val, new_val, updated_at)
VALUES ('user002', 'city', 'SEOUL', 'BUSAN', NOW() - INTERVAL 60 DAY);

-- user003의 소개글 변경 이력
INSERT INTO profile_update_history (user_id, field_name, old_val, new_val, updated_at)
VALUES ('user003', 'introduction',
        '비트 메이커입니다.',
        '힙합 비트 만들어요. 콜라보 환영합니다!',
        NOW() - INTERVAL 15 DAY);

-- ====================================
-- 3. 데이터 확인 쿼리
-- ====================================

-- 샘플 사용자 목록
SELECT
    u.user_id,
    u.nickname,
    u.city,
    u.sex,
    u.is_public,
    GROUP_CONCAT(DISTINCT g.genre_name) as genres,
    GROUP_CONCAT(DISTINCT i.instrument_name) as instruments
FROM user_info u
LEFT JOIN user_genres ug ON u.user_id = ug.user_id
LEFT JOIN genre_name g ON ug.genre_id = g.genre_id
LEFT JOIN user_instruments ui ON u.user_id = ui.user_id
LEFT JOIN instrument_name i ON ui.instrument_id = i.instrument_id
GROUP BY u.user_id, u.nickname, u.city, u.sex, u.is_public
ORDER BY u.created_at DESC;

-- 지역별 사용자 수
SELECT
    l.city_name,
    COUNT(u.user_id) as user_count
FROM location_names l
LEFT JOIN user_info u ON l.city_id = u.city
GROUP BY l.city_id, l.city_name
ORDER BY user_count DESC;

-- 장르별 인기도
SELECT
    g.genre_name,
    COUNT(ug.user_id) as user_count
FROM genre_name g
LEFT JOIN user_genres ug ON g.genre_id = ug.genre_id
GROUP BY g.genre_id, g.genre_name
ORDER BY user_count DESC;

-- 악기별 인기도
SELECT
    i.instrument_name,
    COUNT(ui.user_id) as user_count
FROM instrument_name i
LEFT JOIN user_instruments ui ON i.instrument_id = ui.instrument_id
GROUP BY i.instrument_id, i.instrument_name
ORDER BY user_count DESC;

-- ====================================
-- 4. 정리 (샘플 데이터 삭제)
-- ====================================

-- 샘플 사용자 삭제 (CASCADE로 인해 관련 데이터도 자동 삭제됨)
-- DELETE FROM user_info WHERE user_id LIKE 'user%';
