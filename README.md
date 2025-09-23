![](https://velog.velcdn.com/images/rlagywnd05/post/587b98a9-c7dd-483e-81dc-c9630d4a35b8/image.png)


```
Cafe-Persona-Project/
├── build.gradle / settings.gradle       # Gradle 설정
├── src/
│   ├── main/
│   │   ├── java/com/team/cafe/
│   │   │   ├── CafePersonaProjectApplication.java   # 메인 클래스
│   │   │
│   │   │   ├── cafe/        # 도메인: 카페
│   │   │   │   ├── Cafe.java
│   │   │   │   ├── CafeRepository.java
│   │   │   │   ├── CafeService.java
│   │   │   │   └── CafeController.java
│   │   │   │
│   │   │   ├── review/      # 도메인: 리뷰
│   │   │   │   ├── Review.java
│   │   │   │   ├── ReviewImage.java
│   │   │   │   ├── ReviewReport.java
│   │   │   │   ├── ReviewRepository.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   └── ReviewController.java
│   │   │   │
│   │   │   ├── keyword/     # 도메인: 키워드/태그
│   │   │   │   ├── Keyword.java
│   │   │   │   ├── KeywordType.java
│   │   │   │   ├── CafeKeyword.java
│   │   │   │   └── KeywordRepository.java
│   │   │   │
│   │   │   ├── bookmark/
│   │   │   │   ├── Bookmark.java
│   │   │   │   ├── BookmarkRepository.java
│   │   │   │   └── BookmarkService.java
│   │   │   │
│   │   │   ├── like/
│   │   │   │   ├── ReviewLike.java
│   │   │   │   ├── CafeLike.java
│   │   │   │   └── LikeService.java
│   │   │   │
│   │   │   ├── user/        # 도메인: 사용자
│   │   │   │   ├── SiteUser.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   ├── common/      # 공용 기능
│   │   │   │   ├── BaseTimeEntity.java
│   │   │   │   └── exception/ (예외 처리)
│   │   │   │
│   │   │   └── config/
│   │   │       ├── WebConfig.java       # 정적 리소스 매핑
│   │   │       └── SecurityConfig.java  # Spring Security 설정
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties / application.yml
│   │   │   ├── templates/   # Thymeleaf 뷰
│   │   │   │   ├── layout/
│   │   │   │   │   ├── navbar.html
│   │   │   │   │   └── base.html
│   │   │   │   ├── cafe/
│   │   │   │   │   ├── list.html
│   │   │   │   │   ├── detail.html
│   │   │   │   │   └── form.html
│   │   │   │   ├── review/
│   │   │   │   │   ├── form.html
│   │   │   │   │   └── list.html
│   │   │   │   └── user/
│   │   │   │       ├── login.html
│   │   │   │       └── signup.html
│   │   │   └── static/      # CSS/JS/이미지
│   │   │       ├── css/
│   │   │       ├── js/
│   │   │       └── images/
│   │   └── webapp/ (필요시)
│   │
│   └── test/java/com/team/cafe/   # 단위 테스트
└── README.md
```



# 카페 프로젝트 요구사항서 

> 목적: 사용자의 취향/상황(페르소나·태그)에 맞춰 카페를 탐색·비교·저장·리뷰할 수 있는 웹 서비스 구축. Spring Boot + JPA + Thymeleaf + MariaDB를 기반으로 MVP를 완성하고, 이후 고도화를 위한 기준 문서로 활용한다.

---

## 1) 프로젝트 개요

* **제품명(가칭)**: Bean spot Project
* **핵심 가치**: 상황·취향 맞춤 카페 탐색(태그 AND 검색), 신뢰 가능한 리뷰·이미지, 북마크/좋아요 저장
* **플랫폼**: Web(SSR: Thymeleaf)
* **기술 스택**: Spring Boot, Spring Security, JPA(Hibernate), MariaDB/MySQL, Thymeleaf, jQuery(선택), Bootstrap/Tailwind(선택)

---

## 2) 사용자 및 페르소나

* **커플/데이트**: 분위기·사진 포인트·조용함, 루프탑/야경 등
* **가족(아이 동반)**: 키즈존/주차/유아 메뉴
* **공부/작업**: 콘센트/와이파이/조용함/장시간
* **여성 이용자**: 디저트·라떼아트·포토존·건강 메뉴
* **남성 이용자**: 커피 전문성·브런치·테라스·드라이브 동선
* **풍경/여행**: 루프탑/시골뷰/드라이브 코스
* **기타**: 반려동물 동반, 심야 영업, 비건, 전시/공연, 팝업


---

## 3) 범위(Scope)

### MVP 포함(Must)

* 회원가입/로그인/로그아웃(세션)
* 카페 목록/검색/필터(태그·주차·영업중)
* 카페 상세(이미지, 기본정보, 영업시간, 위치, 평균평점, 리뷰 수, 좋아요/북마크 수)
* 리뷰 CRUD, 리뷰 이미지 업로드(대표 지정/정렬)
* 좋아요/북마크 토글 + 수 집계
* 키워드/태그 관리 및 필터링(유형별 그룹)
* 카드 리스트 성능 최적화(배치 API로 이미지/좋아요/평점/북마크 묶음)
* 소셜 로그인(카카오)
* 맵 위치인증 기반 리뷰작성
* 구글Place Api 카페초기 데이터 작성 (카페명, 주소 등)

### 차기 포함(Should)

* 사업자/관리자 화면(카페 정보/메뉴 관리)
* 리뷰 신고/처리(Report)
* 사용자 성향 테스트



---

## 4) 기능 요구사항

### 4.1 인증/사용자

* 이메일/닉네임/비밀번호 기반 가입, 비밀번호 재설정(토큰)
* 로그인 없이도 목록/상세/검색 가능, **리뷰/좋아요/북마크는 로그인 필요**
* 권한: `ROLE_USER`, `ROLE_OWNER`, `ROLE_ADMIN`

### 4.2 카페 목록/검색/필터

* 파라미터: `kw, page, size, sort, dir, parking(Boolean), openNow(Boolean), keyList(List<Long>)`
* 정렬: 최신순(default), 이름순, 인기순
* **영업 중 판정 로직**

  * 일반 케이스: `openTime <= closeTime` → `open ≤ now ≤ close`
  * 야간영업(자정 넘김): `openTime > closeTime` → `now ≥ open` OR `now ≤ close`
* 결과 카드에는: 대표 이미지, 카페명, 주소(도시/구/상세), 영업시간, 평점 평균, 좋아요/북마크 수
* **배치 통계 API**로 카드에 필요한 값 일괄 조회(IDs)

### 4.3 카페 상세

* 기본정보 + 소개글 + 위치 + 영업시간 + 태그(칩)
* 카페 이미지 갤러리(대표/정렬), 좋아요/북마크 토글 상태, 평균 평점/리뷰 수
* 리뷰 리스트(최근순/평점순), 메뉴 목록

### 4.4 좋아요 & 북마크

* 개념 분리: `CafeLike` / `Bookmark` 별도 조인 엔티티
* 유니크: `(user_id, cafe_id)` 중복 불가
* 토글 응답: `{ liked/bookmarked, count }`
* **정책 옵션**: 좋아요 성공 시 자동 북마크 생성(서비스에서 `ensureExists`)

### 4.5 리뷰/이미지/신고

* 리뷰: 평점(1\~5), 본문(TEXT), 작성·수정·삭제(작성자/관리자)
* 리뷰이미지: `multipart/form-data` 업로드, 대표 1개 보장, 정렬
* 리뷰신고(차기): 사유(enum), 상세, 처리상태(PENDING/APPROVED/REJECTED)

### 4.6 키워드/태그

* `KeywordType`(페르소나/공간특성/메뉴특성 등)
* `CafeKeyword`로 카페-키워드 연결(중복 방지)
* **검색 로직**: 3태그 AND → 미존재시 2태그 매칭 fallback →인기순/이름순 정렬

ex) 3개의 키워드 선택
사용자 선택 태그: [데이트, 루프탑, 디저트]

1차: AND 검색(3/3 매칭) - (1차 까지만 구현)

세 태그를 모두 가진 카페만 노출.

결과가 0개면 2차: 완화 검색(2/3 매칭)

세 태그 중 적어도 2개를 가진 카페를 노출.

### 4.7 사업자/관리자(차기)

* 카페 정보/메뉴/이미지 관리(OWNER)
* 신고 처리/사용자 관리(ADMIN)

---

## 5) 비기능 요구사항(NFR)


* **보안**: 비밀번호 해시(BCrypt), CSRF 방어, 인증 필요 API 보호, 파일 업로드 확장자

---


## 6) 화면/UX 요구사항

* **공통 테마**: 따뜻하고 미니멀한 카페 톤, 라운드·은은한 그림자, 칩 컴포넌트
* **목록**: 4×3 카드 그리드(데스크톱), 반응형(모바일 2열)
* **검색바/필터**: 상단 고정, 체크 시 선택 칩 노출, 초기화 버튼
* **상세**: 헤더(대표 이미지/기본정보/토글), 탭(정보/리뷰/사진)
* **접근성/피드백**: 토글/업로드 시 스낵바/아이콘 상태 전환

---

## 7) 정책/규칙

* 리뷰 평점: 1\~5 정수(서버 검증)
* 좋아요·북마크: 사용자당 카페 1개만(UNIQUE)
* 리뷰: (정책 선택) 사용자당 카페 다수, 다수 허용 시 수정 이력 보관 고려
* 이미지: 한 리뷰에 대표 1개 보장(서비스 or DB 제약)
* 검색 태그: 최대 3개 선택, fallback 2개 매칭
* 조회수(views) 상세 페이지 렌더링 시 1회 처리
	로그인 사용자: (userId, cafeId) 기준 12시간 내 재방문은 미측정
	비로그인 사용자: (clientId 쿠키, cafeId) 기준 12시간 내 미측정.
    첫 방문 시     clientId 발급(1년 만료)


---


## 8) 테스트 기준

### 8.1 카페 목록 검색

* [ ] `kw`가 이름/주소/도시에 부분 일치하면 노출된다
* [ ] `parking=true`면 주차 가능한 카페만 노출된다
* [ ] `openNow=true`면 현재 영업 중인 카페만 노출된다(자정 넘김 케이스 포함)
* [ ] `keyList` 선택 갯수만큼 AND 매칭, 현재 완화조건 없음

### 8.2 좋아요/북마크 토글

* [ ] 로그인하지 않으면 401/리다이렉트 처리한다
* [ ] 최초 클릭 시 생성되고, 재클릭 시 해제된다
* [ ] 응답에 최신 카운트가 반환된다
* [ ] 좋아요 성공 시 북마크가 보장된다

### 8.3 리뷰/이미지

* [ ] 평점 1\~5 이외면 400을 반환한다
* [ ] 작성자는 본인 리뷰만 수정/삭제할 수 있다
* [ ] 이미지 업로드는 허용 확장자/최대 용량을 초과하면 거부한다
* [ ] 대표 이미지를 변경하면 기존 대표는 자동 해제된다

---
