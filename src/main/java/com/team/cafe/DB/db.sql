# DB 생성
DROP DATABASE IF EXISTS cafe_dev;
CREATE DATABASE cafe_dev;
USE cafe_dev;

show tables;
desc bookmark;
desc cafe;
desc cafe_keyword;
desc keyword;
desc keyword_type;
desc liked_users;
desc review;
desc review_like;
desc site_user;

INSERT INTO cafe (cafe_name, address1, city, district, phone_num,
                  open_time, close_time, parking_yn, hit_count, created_at, updated_at)
VALUES
('라떼하우스', '대전 서구 둔산동 101', '대전', '서구', '010-1111-1111',
 '09:00:00', '22:00:00', 1, 0, NOW(), NOW()),
('브루잉랩', '대전 유성구 전민동 202', '대전', '유성구', '010-2222-2222',
 '08:30:00', '21:30:00', 0, 0, NOW(), NOW()),
('카페 모노', '대전 중구 중앙로 303', '대전', '중구', '010-3333-3333',
 '10:00:00', '20:00:00', 1, 0, NOW(), NOW()),
('블루빈스', '서울 강남구 테헤란로 123', '서울', '강남구', '02-111-2222',
 '09:00:00', '23:00:00', 1, 0, NOW(), NOW()),
('커피스미스', '서울 마포구 홍대입구 45', '서울', '마포구', '02-333-4444',
 '07:30:00', '23:30:00', 0, 0, NOW(), NOW()),
('모닝브루', '부산 해운대구 해운대해변로 30', '부산', '해운대구', '051-123-4567',
 '08:00:00', '22:00:00', 1, 0, NOW(), NOW()),
('카페 솔향', '강릉시 경포로 77', '강원', '강릉시', '033-777-8888',
 '09:00:00', '21:00:00', 0, 0, NOW(), NOW()),
('빈센트커피', '광주 동구 충장로 55', '광주', '동구', '062-222-3333',
 '08:00:00', '22:00:00', 1, 0, NOW(), NOW()),
('카페 달빛', '대구 중구 동성로 88', '대구', '중구', '053-123-4567',
 '10:00:00', '23:00:00', 1, 0, NOW(), NOW()),
('카페 미드나잇', '인천 남동구 예술로 12', '인천', '남동구', '032-987-6543',
 '09:30:00', '21:00:00', 0, 0, NOW(), NOW()),
('코지커피', '세종특별자치시 한누리대로 100', '세종', '세종시', '044-123-0000',
 '08:30:00', '20:30:00', 1, 0, NOW(), NOW()),
('카페 스노우', '제주 서귀포시 중문로 50', '제주', '서귀포시', '064-333-5555',
 '09:00:00', '22:00:00', 1, 0, NOW(), NOW()),
('카페 하늘', '전주 완산구 전주로 77', '전북', '전주시', '063-123-7890',
 '08:00:00', '21:00:00', 0, 0, NOW(), NOW()),
('빈브라운', '울산 남구 삼산로 12', '울산', '남구', '052-222-7777',
 '10:00:00', '22:00:00', 1, 0, NOW(), NOW()),
('카페 선셋', '포항 북구 해맞이로 99', '경북', '포항시', '054-888-2222',
 '09:00:00', '20:00:00', 0, 0, NOW(), NOW()),
('카페 에코', '창원 성산구 용지로 101', '경남', '창원시', '055-123-3333',
 '08:30:00', '21:30:00', 1, 0, NOW(), NOW()),
('카페 민트', '춘천시 석사동 50', '강원', '춘천시', '033-555-6666',
 '09:00:00', '23:00:00', 0, 0, NOW(), NOW()),
('카페 루프탑', '서울 용산구 이태원로 200', '서울', '용산구', '02-999-1111',
 '11:00:00', '23:59:59', 1, 0, NOW(), NOW()),
('카페 숲', '수원 영통구 대학로 88', '경기', '수원시', '031-321-9876',
 '08:00:00', '21:00:00', 1, 0, NOW(), NOW()),
('카페 온도', '성남 분당구 정자일로 55', '경기', '성남시', '031-555-1234',
 '09:00:00', '22:00:00', 0, 0, NOW(), NOW());

INSERT INTO cafe (cafe_name, address1, city, district, phone_num,
                  open_time, close_time, parking_yn, hit_count, created_at, updated_at)
VALUES
('브루잉데이', '대전 서구 둔산동 102', '대전', '서구', '010-2222-1111',
 '08:00:00', '21:00:00', 1, 12, NOW(), NOW()),
('카페모먼트', '대전 서구 월평동 45-1', '대전', '서구', '010-3333-1111',
 '10:00:00', '23:00:00', 0, 7, NOW(), NOW()),
('더커피랩', '대전 유성구 궁동 12-3', '대전', '유성구', '010-4444-1111',
 '09:30:00', '22:00:00', 1, 18, NOW(), NOW()),
('블루포레스트', '대전 중구 은행동 5-7', '대전', '중구', '010-5555-1111',
 '09:00:00', '21:30:00', 0, 5, NOW(), NOW()),
('라임라이트', '대전 동구 용전동 88', '대전', '동구', '010-6666-1111',
 '11:00:00', '23:30:00', 1, 30, NOW(), NOW()),
('빈스테이션', '대전 대덕구 법동 123', '대전', '대덕구', '010-7777-1111',
 '08:30:00', '22:00:00', 0, 3, NOW(), NOW()),
('카페소소', '대전 서구 탄방동 202', '대전', '서구', '010-8888-1111',
 '09:00:00', '22:00:00', 1, 11, NOW(), NOW()),
('하루한잔', '대전 유성구 봉명동 45-12', '대전', '유성구', '010-9999-1111',
 '10:00:00', '23:00:00', 1, 8, NOW(), NOW()),
('카페드림', '대전 중구 선화동 67-3', '대전', '중구', '010-1212-1111',
 '09:00:00', '21:00:00', 0, 25, NOW(), NOW()),
('에스프레소바', '대전 동구 성남동 77', '대전', '동구', '010-1313-1111',
 '09:00:00', '22:30:00', 1, 14, NOW(), NOW()),
('온더브루', '대전 대덕구 오정동 34-2', '대전', '대덕구', '010-1414-1111',
 '08:30:00', '22:00:00', 1, 19, NOW(), NOW()),
('카페루프탑', '대전 서구 둔산동 305', '대전', '서구', '010-1515-1111',
 '10:00:00', '23:00:00', 0, 21, NOW(), NOW()),
('스윗빈', '대전 유성구 신성동 56-8', '대전', '유성구', '010-1616-1111',
 '09:00:00', '22:00:00', 1, 27, NOW(), NOW()),
('카페온에어', '대전 중구 문화동 145', '대전', '중구', '010-1717-1111',
 '08:00:00', '21:00:00', 0, 6, NOW(), NOW()),
('티라떼하우스', '대전 동구 가오동 89', '대전', '동구', '010-1818-1111',
 '09:30:00', '22:30:00', 1, 9, NOW(), NOW()),
('브라운브릭스', '대전 대덕구 송촌동 101', '대전', '대덕구', '010-1919-1111',
 '10:00:00', '23:00:00', 0, 4, NOW(), NOW()),
('카페테라스', '대전 서구 둔산동 450', '대전', '서구', '010-2020-1111',
 '09:00:00', '22:00:00', 1, 15, NOW(), NOW()),
('리프앤빈', '대전 유성구 죽동 212', '대전', '유성구', '010-2121-1111',
 '08:00:00', '21:30:00', 0, 16, NOW(), NOW()),
('카페코지', '대전 중구 대흥동 33-1', '대전', '중구', '010-2223-1111',
 '09:00:00', '22:00:00', 1, 13, NOW(), NOW()),
('데일리브루', '대전 동구 홍도동 62', '대전', '동구', '010-2323-1111',
 '10:00:00', '23:00:00', 1, 28, NOW(), NOW());


INSERT INTO site_user ( last_login, role, nickname, username, email, password)
VALUES
  ( NOW(6), 'USER', '효중', 'hj', 'hj@example.com', '1234'),
  ( NULL, 'USER', '정성', 'js', 'js@example.com', '1234'),
  ( NOW(6), 'ADMIN', '관리자', 'admin1', 'admin@example.com', '1234'),
  ( NULL, 'USER', '현영', 'hy', 'hy@example.com', '1234'),
  ( NOW(6), 'USER', '상진', 'sj', 'sj@example.com', '1234');


INSERT INTO cafe_image (cafe_id, img_url, created_at, updated_at)
VALUES
(2,  '/images/DBimg.png', now(), now()),
(1,  '/images/DBimg.png', now(), now()),
(3,  '/images/DBimg.png', now(), now()),
(4,  '/images/DBimg.png', now(), now()),
(5,  '/images/DBimg.png', now(), now()),
(6,  '/images/DBimg.png', now(), now()),
(7,  '/images/DBimg.png', now(), now()),
(8,  '/images/DBimg.png', now(), now()),
(9,  '/images/DBimg.png', now(), now()),
(10, '/images/DBimg.png', now(), now()),
(11, '/images/DBimg.png', now(), now()),
(12, '/images/DBimg.png', now(), now()),
(13, '/images/DBimg.png', now(), now()),
(14, '/images/DBimg.png', now(), now()),
(15, '/images/DBimg.PNG', now(), now()),
(16, '/images/DBimg.PNG', now(), now()),
(17, '/images/DBimg.PNG', now(), now()),
(18, '/images/DBimg.PNG', now(), now()),
(19, '/images/DBimg.PNG', now(), now()),
(20, '/images/DBimg.PNG', now(), now());



select * from business;
select * from cafe_image;
select * from site_user;
desc site_user;
select * from bookmark;
select * from cafe;
-- TRUNCATE TABLE cafe;
-- DELETE FROM cafe_image;
-- DELETE FROM cafe;
desc site_user;
drop table site_user;

— site_user 테이블
CREATE TABLE IF NOT EXISTS site_user (
    user_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    rrn VARCHAR(13),
    UNIQUE KEY uk_siteuser_username (username),
    UNIQUE KEY uk_siteuser_email (email),
    INDEX idx_siteuser_username (username),
    INDEX idx_siteuser_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

— business 테이블
CREATE TABLE IF NOT EXISTS business (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    business_number VARCHAR(50) NOT NULL UNIQUE,
    representative_name VARCHAR(100),
    representative_phone VARCHAR(50),
    representative_email VARCHAR(255),
    address VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES site_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


select * from keyword;
select * from cafe_keyword;
select * from keyword_type;
desc cafe_keyword;
desc keyword;
desc keyword_type;

INSERT INTO keyword_type (id, type_name) VALUES
(1, 'PERSONA'),   -- 이용자 성향
(2, 'SPACE'),     -- 공간/시설
(3, 'MENU'),      -- 메뉴/음료
(4, 'FEATURE');   -- 기타 특징/분위기



-- PERSONA
INSERT INTO keyword (id, name, type_id) VALUES
(1, '데이트', 1),
(2, '가족/아이', 1),
(3, '공부/작업', 1),
(4, '친구모임', 1),
(5, '여행코스', 1);

-- SPACE
INSERT INTO keyword (id, name, type_id) VALUES
(6, '루프탑', 2),
(7, '포토존', 2),
(8, '조용한 공간', 2),
(9, '와이드 좌석', 2),
(10, '콘센트 많음', 2),
(11, '키즈존', 2),
(12, '반려동물 동반', 2),
(13, '넓은 주차장', 2),
(14, '테라스', 2);

-- MENU
INSERT INTO keyword (id, name, type_id) VALUES
(15, '디저트 맛집', 3),
(16, '브런치', 3),
(17, '핸드드립', 3),
(18, '싱글오리진', 3),
(19, '말차/라떼', 3),
(20, '비건 메뉴', 3);

-- FEATURE
INSERT INTO keyword (id, name, type_id) VALUES
(21, '야외뷰/풍경', 4),
(22, '시즌 데코', 4),
(23, '심야 영업', 4),
(24, '감성 인테리어', 4),
(25, '조명 좋은', 4),
(26, '전시/굿즈', 4);

-- Cafe 1: 데이트 + 루프탑 + 디저트
INSERT INTO cafe_keyword (cafe_id, keyword_id) VALUES
(1, 1),   -- 데이트
(1, 6),   -- 루프탑
(1, 15);  -- 디저트 맛집

-- Cafe 2: 가족 단위 + 키즈존 + 주차장 + 브런치
INSERT INTO cafe_keyword (cafe_id, keyword_id) VALUES
(2, 2),   -- 가족/아이
(2, 11),  -- 키즈존
(2, 13),  -- 넓은 주차장
(2, 16);  -- 브런치

-- Cafe 3: 공부/작업 + 콘센트 + 감성 인테리어
INSERT INTO cafe_keyword (cafe_id, keyword_id) VALUES
(3, 3),   -- 공부/작업
(3, 10),  -- 콘센트 많음
(3, 24);  -- 감성 인테리어

