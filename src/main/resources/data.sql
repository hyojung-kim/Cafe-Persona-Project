// users (회원)

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,                // 회원 고유 ID (PK)
    name VARCHAR(100) NOT NULL,                       // 회원 이름
    email VARCHAR(255) NOT NULL UNIQUE,               // 이메일
    password_hash VARCHAR(20) NOT NULL,               // 비밀번호
    role VARCHAR(20) DEFAULT 'user',                  // 회원 유형
    status VARCHAR(20) DEFAULT 'active',              // 계정 상태
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   // 가입일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP // 수정일
)







// business(사업자 정보) 테이블

CREATE TABLE business (
    id INT AUTO_INCREMENT PRIMARY KEY,                 // 사업자 고유 ID (PK)
    company_name VARCHAR(255) NOT TULL,                // 회사명
    business_number VARCHAR(50) NOT NULL UNIQUE,       // 사업자등록번호
    representative_name VARCHAR(100),                  // 대표자명
    representative_phone VARCHAR(50),                  // 대표자 연락처
    representative_email VARCHAR(255),                 // 대표자 이메일
    address VARCHAR(255),                              // 주소
    status VARCHAR(20) DEFAULT 'pending',              // 승인 상태
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     // 등록일
    update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  // 수정일
    FOREIGN KEY (user_id) REFERENCES user(id)          // user 테이블 fk
)

// business_term_agreement (사업자 약관 동의 내역)

CREATE TABLE business_term_agreement (
    id INT AUTO_INCREMENT PRIMARY KEY,                  // 약관 동의 고유 ID (PK)
    business_id INT NOT NULL,                           // business.id (FK), 어떤 사업자가 동의했는지
    term_name VARCHAR(255) NOT NULL,                    // 약관 이름
    agreed BOOLEAN NOT NULL,                            // 동의 여부 (true/false)
    agreed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      // 동의한 시간
    FOREIGN KEY (business_id) REFERENCES business(id)   // business 테이블과 FK 관계
);