
-- 사용자 정보
CREATE TABLE IF NOT EXISTS user_info (
    user_id VARCHAR(255) PRIMARY KEY,
    profile_image_url VARCHAR(255),
    sex VARCHAR(1),
    nickname VARCHAR(100) NOT NULL UNIQUE,
    city enum('ETC','강원','경기','경남','경북','광주','대구','대전','부산','서울','울산','인천','전남','전북','제주','충남','충북') DEFAULT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    last_updated_at TIMESTAMP,
    is_public BOOLEAN,
    is_chatable BOOLEAN
);

-- 프로필 변경 이력
CREATE TABLE IF NOT EXISTS profile_update_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    updated_at TIMESTAMP NOT NULL,
    old_val VARCHAR(255),
    new_val VARCHAR(255),
    field_name VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id)
        REFERENCES user_info(user_id)
        ON DELETE CASCADE
);

-- 악기 명칭 테이블
CREATE TABLE IF NOT EXISTS instrument_name (
    instrument_id INT PRIMARY KEY,
    instrument_name VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 0
);

-- 사용자-악기 매핑 (EmbeddedId: user_id + instrumentId) + 낙관적 락 version
CREATE TABLE IF NOT EXISTS user_instruments (
                                                user_id VARCHAR(255) NOT NULL,
                                                instrument_id INT NOT NULL,
                                                version INT NOT NULL DEFAULT 0,
                                                PRIMARY KEY (user_id, instrument_id),
                                                CONSTRAINT fk_instruments_user FOREIGN KEY (user_id)
                                                    REFERENCES user_info(user_id)
                                                    ON DELETE CASCADE,
                                                CONSTRAINT fk_instruments_instrument FOREIGN KEY (instrument_id)
                                                    REFERENCES instrument_name(instrument_id)
);

-- 장르 명칭 테이블 (주의: 엔티티 컬럼명이 genreId)
CREATE TABLE IF NOT EXISTS genre_name (
                                          genre_id INT PRIMARY KEY,
                                          genre_name VARCHAR(255) NOT NULL,
                                          version INT NOT NULL DEFAULT 0
);


-- 사용자-장르 매핑 (EmbeddedId: user_id + genreId)
-- 엔티티에 별도 컬럼 genre_id 도 존재하므로 함께 생성
CREATE TABLE IF NOT EXISTS user_genres (
                                           user_id VARCHAR(255) NOT NULL,
                                           genre_id INT NOT NULL,
                                           version INT NOT NULL DEFAULT 0,
                                           PRIMARY KEY (user_id, genre_id),
                                           CONSTRAINT fk_user_genres_user FOREIGN KEY (user_id)
                                               REFERENCES user_info(user_id)
                                               ON DELETE CASCADE,
                                           CONSTRAINT fk_user_genres_genre FOREIGN KEY (genre_id)
                                               REFERENCES genre_name(genre_id)
);


-- auth.shedlock definition

CREATE TABLE IF NOT EXISTS shedlock
(
    `name`       varchar(64)  NOT NULL,
    `lock_until` timestamp(3) NOT NULL,
    `locked_at`  timestamp(3) NOT NULL,
    `locked_by`  varchar(255) NOT NULL,
    PRIMARY KEY (`name`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;
