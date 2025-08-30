# DB 생성
DROP DATABASE IF EXISTS cafe_dev;
CREATE DATABASE cafe_dev;
USE cafe_dev;

show tables;
desc bookmark;
desc cafe;
desc cafe_keyword;
desc cafe_like;
desc keyword;
desc keyword_type;
desc review;
desc review_like;
desc site_user;

INSERT INTO cafe (cafe_id, cafe_name, address1, city, district, phone_num, site_url,
                  open_time, close_time, parking_yn, lat, lng, hit_count, created_at, updated_at)
VALUES
(1,'라떼하우스','대전 서구 둔산동 123','대전','서구','010-1111-2222','https://example.com',
 '09:00:00','22:00:00',1,36.3512345,127.3845678,0,NOW(),NOW()),
(2,'카페 모노','대전 유성구 어딘가 45','대전','유성구','010-3333-4444',NULL,
 '10:00:00','21:00:00',0,36.3612345,127.3745678,0,NOW(),NOW());


INSERT INTO site_user (created_at, last_login, role, nickname, username, email, password)
VALUES
  (NOW(6), NOW(6), 'USER', '효중', 'hj', 'hj@example.com', '1234'),
  (NOW(6), NULL, 'USER', '정성', 'js', 'js@example.com', '1234'),
  (NOW(6), NOW(6), 'ADMIN', '관리자', 'admin1', 'admin@example.com', '1234'),
  (NOW(6), NULL, 'USER', '현영', 'hy', 'hy@example.com', '1234'),
  (NOW(6), NOW(6), 'USER', '상진', 'sj', 'sj@example.com', '1234');


select * from site_user;
desc site_user;

select * from cafe;
-- DELETE FROM site_user;
