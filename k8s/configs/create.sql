CREATE SCHEMA IF NOT EXISTS `bungaebowling_db` DEFAULT CHARACTER SET utf8mb4;

USE `bungaebowling_db`;

DROP TABLE IF EXISTS `user_tb`;
CREATE TABLE user_tb
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(20)  NOT NULL UNIQUE,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password         VARCHAR(100) NOT NULL,
    district_id      BIGINT       NOT NULL,
    img_url          VARCHAR(200),
    result_image_url VARCHAR(200),
    role             VARCHAR(50) DEFAULT 'ROLE_PENDING' CHECK (role IN ('ROLE_PENDING', 'ROLE_USER')),
    created_at       TIMESTAMP   DEFAULT now()
);

DROP TABLE IF EXISTS `post_tb`;
CREATE TABLE post_tb
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    user_id     BIGINT       NOT NULL,
    district_id BIGINT       NOT NULL,
    content     TEXT         NOT NULL,
    start_time  DATETIME     NOT NULL,
    is_close    BOOLEAN   DEFAULT FALSE,
    due_time    DATETIME     NOT NULL,
    view_count  INT       DEFAULT 0,
    edited_at   TIMESTAMP DEFAULT now(),
    created_at  TIMESTAMP DEFAULT now()
);

DROP TABLE IF EXISTS `comment_tb`;
CREATE TABLE comment_tb
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT,
    parent_id  BIGINT,
    content    TEXT,
    edited_at  TIMESTAMP DEFAULT now(),
    created_at TIMESTAMP DEFAULT now()
);

DROP TABLE IF EXISTS `applicant_tb`;
CREATE TABLE applicant_tb
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    status     BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now(),
    UNIQUE (post_id, user_id)
);

DROP TABLE IF EXISTS `score_tb`;
CREATE TABLE score_tb
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    post_id          BIGINT NOT NULL,
    score_num        INT    NOT NULL,
    result_image_url VARCHAR(100),
    access_image_url VARCHAR(200),
    created_at       TIMESTAMP DEFAULT now()
);

DROP TABLE IF EXISTS `user_rate_tb`;
CREATE TABLE user_rate_tb
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    star_count   INT    NOT NULL,
    created_at   TIMESTAMP DEFAULT now()
);

DROP TABLE IF EXISTS `message_tb`;
CREATE TABLE message_tb
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT  NOT NULL,
    opponent_user_id BIGINT  NOT NULL,
    is_receive       BOOLEAN NOT NULL,
    is_read          BOOLEAN   DEFAULT false,
    content          TEXT    NOT NULL,
    created_at       TIMESTAMP DEFAULT now()
);

DROP TABLE IF EXISTS `city_tb`;
CREATE TABLE city_tb
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS `country_tb`;
CREATE TABLE country_tb
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT      NOT NULL,
    name    VARCHAR(50) NOT NULL,
    UNIQUE (city_id, name)
);

DROP TABLE IF EXISTS `district_tb`;
CREATE TABLE district_tb
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    statutory_code BIGINT      NOT NULL,
    country_id     BIGINT      NOT NULL,
    name           VARCHAR(50) NOT NULL,
    UNIQUE (country_id, name)
);

