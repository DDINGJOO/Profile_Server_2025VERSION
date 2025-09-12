CREATE TABLE IF NOT EXISTS user_info (
                                         id VARCHAR(255) PRIMARY KEY NOT NULL,
    nickname VARCHAR(100) NOT NULL UNIQUE,
    profile_image_url VARCHAR(255) NULL,
    bio VARCHAR(100) NULL,
    sex TINYINT NULL,
    location INT NULL,
    version INT NULL,
    created_at TIMESTAMP NULL,
    last_updated_at TIMESTAMP NULL,
    is_public TINYINT NULL,
    is_chatable TINYINT NULL
    );

CREATE TABLE IF NOT EXISTS user_info_history (
                                                 history_id VARCHAR(255) PRIMARY KEY NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    version INT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    column_name VARCHAR(45) NOT NULL,
    old_value VARCHAR(255) NULL,
    new_value VARCHAR(255) NULL,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES user_info(id)
    );

-- 악기 목록(참조용)
CREATE TABLE IF NOT EXISTS instrument_name (
                                               instrument_id INT PRIMARY KEY NOT NULL,
                                               instrument_name VARCHAR(255) NOT NULL
    );

-- 사용자-악기 매핑 (다대다, 복합 PK)
CREATE TABLE IF NOT EXISTS user_instruments (
                                                user_id VARCHAR(255) NOT NULL,
    instrument_id INT NOT NULL,
    PRIMARY KEY (user_id, instrument_id),
    CONSTRAINT fk_ui_user FOREIGN KEY (user_id) REFERENCES user_info(id),
    CONSTRAINT fk_ui_instrument FOREIGN KEY (instrument_id) REFERENCES instrument_name(instrument_id)
    );

-- 장르 목록(참조용)
CREATE TABLE IF NOT EXISTS genre_name (
                                          genre_id INT PRIMARY KEY NOT NULL,
                                          genre_name VARCHAR(255) NOT NULL
    );

-- 사용자-장르 매핑 (다대다, 복합 PK)
CREATE TABLE IF NOT EXISTS user_genres (
                                           user_id VARCHAR(255) NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (user_id, genre_id),
    CONSTRAINT fk_ug_user FOREIGN KEY (user_id) REFERENCES user_info(id),
    CONSTRAINT fk_ug_genre FOREIGN KEY (genre_id) REFERENCES genre_name(genre_id)
    );
