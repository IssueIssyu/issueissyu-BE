# issueissyu-BE

issueissyu(이슈있슈) Backend는 지역 기반 핀·커뮤니티·지도 서비스를 제공하는 **Spring Boot 기반 REST API 서버**입니다.  
동네 인증, 핀 등록/조회, 이슈 해결, 커뮤니티 피드, FCM 푸시 알림, 인앱 결제 검증 등을 담당합니다.

## 👥 Team
|                                 BE(팀장)                                  |                                 BE                                  |
|:-----------------------------------------------------------------------:|:-------------------------------------------------------------------:|
| <img width="186" height="217" alt="스크린샷 2026-06-01 114950" src="https://github.com/user-attachments/assets/178c459d-5c32-493d-923c-969592913ce7" /> | <img width="180" height="217" alt="스크린샷 2026-06-01 115059" src="https://github.com/user-attachments/assets/6541f332-a27d-4279-8052-ecf819a4ff2a" /> |
|       김태림<br/><a href="https://github.com/taerimiiii">@taerim</a>       |       전유안<br/><a href="https://github.com/yaaan7">@yaaan</a>        |

## 💻 Tech Stack
- **Framework/Language**: Spring Boot 4.x, Java 21
- **Build/Database**: Gradle, PostgreSQL(PostGIS), Spring Data JPA, Redis
- **Auth & Security**: JWT, Spring Security, OAuth2(Naver), Solapi(SMS 인증)
- **External Services**: AWS S3, Firebase FCM, Naver Maps API, Google Play Billing
- **Docs**: Swagger (SpringDoc)

## **📂 Project Structure**
도메인형 (Domain-driven)
```
issueissyu-BE/
├── .github/                       # Issue/PR 템플릿 및 CI/CD 설정
├── src/main/java/issueissyu/backend/
│   ├── BackendApplication.java
│   ├── global/                    # 전역 공통 모듈
│   │   ├── api/                   # 공통 API 응답 형식
│   │   ├── config/                # Security, Swagger, S3, Redis, FCM, Scheduler 등 설정
│   │   ├── controller/            # 헬스체크 등 전역 컨트롤러
│   │   ├── entity/                # 공통 엔티티(BaseEntity)
│   │   ├── exception/             # 공통 예외 처리
│   │   ├── persistence/           # PostGIS 등 공통 영속성 타입
│   │   ├── redis/                 # Refresh Token Redis 저장
│   │   └── security/              # JWT, OAuth2 인증/인가
│   │
│   ├── domain/                    # 도메인별 패키지
│   │   ├── pin/                   # 핀, 댓글, 좋아요, 이모지
│   │   │   ├── controller/
│   │   │   ├── converter/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── exception/
│   │   ├── alarm/                 # FCM 푸시 알림
│   │   ├── auth/                  # 인증(로컬/네이버 로그인, SMS, 온보딩, 토큰 재발급)
│   │   ├── billing/               # Google Play 인앱 결제 검증
│   │   ├── collection/            # 사용자 커스텀 컬렉션
│   │   ├── community/             # 커뮤니티 피드, 댓글, 좋아요, Hot 승격
│   │   ├── issue/                 # 이슈 핀, 청원, 시민해결사
│   │   ├── location/              # 동네 인증, 좌표→주소, 지역구 조회
│   │   ├── map/                   # 지도 핀 카드, 패치노트, 공지
│   │   └── user/                  # 사용자 프로필, 약관, 알람 설정, 회원 탈퇴
│   │
│   └── utils/                     # S3, Redis 등 유틸
│
├── src/main/resources/
│   └── application.yml            # local / develop 프로필 설정
├── gradle/                        # Gradle Wrapper
├── build.gradle
└── settings.gradle
```

## **🛠️ Architecture**
<img width="1425" height="584" alt="Untitled-Page-2 (15) (2)" src="https://github.com/user-attachments/assets/f998a8e3-db38-4535-8b19-d9b591f7e040" />


## **📝 Commit Convention**
| type | 의미 | 예시 |
| --- | --- | --- |
| ⭐ **feat** | 새로운 기능 | 로그인 API 구현 |
| 🐞 bug | 버그 수정 | NPE 해결 |
| 📖 **docs** | 문서 수정 | README 업데이트 |
| ⚙️ **setting** | 프로젝트/환경 설정 | yml, CI, Gradle 설정 변경 |
| **♻️ refactor** | 기능 변화 없는 코드 리팩터링 | Service 분리 |
| 🎨 **style** | 포맷/세미콜론/네이밍 등 | 포맷팅, 공백 |
| 🧪 **test** | 테스트 코드 | Controller 단위 테스트 |
| 🚀 deploy | 배포, dev→main | 배포 |
