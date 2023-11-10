# Team3_BE

<p align="left">
    <img src="docs/LogoTitle-Orange.png" alt="Logo" width="70%"/>

</p>

번개 지향 볼링 모집 커뮤니티 "번개볼링"의 백엔드 서버입니다.

> [카카오 테크 캠퍼스](https://www.kakaotechcampus.com/) 1기 부산대 3조 프로젝트입니다.

## Introduction

`기존의 볼링 관련 서비스`에서는 `볼링 한판`을 치기 위해서 동호회, 소모임에 가입을 해야하는 `번거로운 과정`이 필요합니다. 소모임, 밴드 앱 또한 일회성의 가벼운 만남이 아닌 주기적으로 참여를 할 회원을 모집하고 있습니다.

**`번개 볼링`은 기존의 소모임, 스포츠 모임 서비스와 다른 번개모임, `빠른 매칭`을 목표로 하고 있습니다.**

---

### 기획

#### 5Whys

<img src="https://github.com/Step3-kakao-tech-campus/Team3_BE/assets/81746373/5084761e-b0af-42f1-9962-a87db722c67c" width="50%"/>

#### 1Pager 기획

<img src="https://github.com/Step3-kakao-tech-campus/Team3_BE/assets/81746373/d7e87208-6fc5-407c-aa7f-5f21442e69b6" width="100%"/>

---

### 둘러보기

- **[실제 배포 링크](https://ka02fa9a0d9a2a.user-app.krampoline.com/)**
- **[api문서](https://bungae.jagaldol.dev:8080/api/docs/swagger)**

### 깃헙 레포지토리

- **[FrontEnd Repository](https://github.com/Step3-kakao-tech-campus/Team3_FE)**
- **[BackEnd Repository](https://github.com/Step3-kakao-tech-campus/Team3_BE)**

## System Structure


### 전체 구성도

<img src="https://github.com/Step3-kakao-tech-campus/Team3_BE/assets/81746373/8e67c097-3aea-410b-bcbc-abeb03eaf9f9" alt="Logo" width="100%"/>

### 백엔드 구성도

<img src="https://github.com/Step3-kakao-tech-campus/Team3_BE/assets/81746373/5742bd39-f728-4d9c-94a8-7cd13f9ded09" alt="Logo" width="100%"/>

### ERD(ER - Diagram) - [ERD 협업 링크](https://www.erdcloud.com/d/GHYAMbQS9pzC6k8ZB)

<img src="https://github.com/Step3-kakao-tech-campus/Team3_BE/assets/81746373/d5f7380f-6ee2-4b68-b94f-c588f40829ec" alt="Logo" width="100%"/>

- 참여신청 테이블(applicant_tb)

  ```text
  승인 상태가 false 인 경우 프론트에서 수락 / 거절 처리합니다.
  거절 시 참여 신청 테이블에서 delete 됩니다.
  수락 시 status가 true가 되면서 수락 / 거절 처리됩니다.
  게시글이 모집완료되면 평가하기 활성화됨 status가 True인 사람들은 게시글에 달려있는 status True인 사람들을 서로 평가할 수 있습니다.
  ```

- 모집글 테이블(comment_tb)

  ```text
  마감 계산은 아래와 같습니다.
  마감 = is_close || (now due_time)
  ```

- 댓글 테이블(comment_tb)

  ```text
  일반 댓글의 경우 부모 댓글id가 NULL입니다.
  대댓글 일 시 부모 댓글id가 존재합니다.
  게시글에 달린 댓글을 전체 조회해서 부모 id에 맞게 조합하여 계층형으로 전달 가능합니다.
  댓글 데이터 삭제 시, delete하지 않고 작성자 id와 내용만 null 처리합니다.
  (부모id를 참조해야하므로 row를 삭제해서는 안됩니다.)
  ```

## Tech Stack

![java 17](https://img.shields.io/badge/-Java%2017-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![spring boot 3.1.3](https://img.shields.io/badge/Spring%20boot%203.1.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![mysql 8.0](https://img.shields.io/badge/MySQL%208.0-005C84?style=for-the-badge&logo=mysql&logoColor=white)

![docker](https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis 6.2](https://img.shields.io/badge/Redis%206.2-DC382D?style=for-the-badge&logo=Redis&logoColor=white)
![Kubernates 1.28.0](https://img.shields.io/badge/KUBERNETES%201.28.0-326CE5?style=for-the-badge&logo=Kubernetes&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)

## How to Start

1. 프로젝트를 클론합니다.

   ```
   $ git clone https://github.com/Step3-kakao-tech-campus/Team3_BE.git
   ```

2. `Temp3_BE/.env.example` 파일을 `.env`로 복사하고 내용을 자신의 환경에 맞게 설정해줍니다.

   ```
   $ cd Team10_BE                       # 디렉토리 이동
   $ cp .env.example .env               # 파일 복사
   $ vi .env                            # .env 수정
   파일 수정 및 저장 진행하기
   ```

   - `.env` 파일은 환경변수를 설정하는 파일입니다.

     ```
     TOKEN_SECRET="example"                 # 로그인시 사용되는 토큰의 시크릿 키를 설정
     DOMAIN="http://localhost:3000"         # 배포될 도메인을 설정
     API_SERVER_URL="http://localhost:8080" # 서버URL 설정
     GMAIL_USERNAME="example@gmail.com"     # 메일인증 등 메일발신에 사용될 GMAIL설정
     GMAIL_APPLICATION_PASSWORD="example"   # 메일인증 등 메일발신에 사용될 GMAIL설정
     AWS_ACCESS_KEY="example"               # AWS S3에 접근하기 위한 키 설정
     AWS_SECRET_KEY="example+9Vkr3fRwA"     # AWS S3에 접근하기 위한 키 설정
     MYSQL_USERNAME="example"               # 데이터 베이스 연결 설정
     MYSQL_PASSWORD="example"               # 데이터 베이스 연결 설정
     GOOGLE_MAP_API_KEY="example"           # 구글맵 API 키 설정
     SSL_KEY_PASSWORD="example"             # SSL 키 설정
     ```

   - local profile을 사용할 시, `SSL`과 `MySQL`의 설정이 불필요합니다.

3. `.env` 환경변수를 등록합니다. (본 가이드에선 우분투 환경으로 진행합니다.)

   ```
   $ set -a
   $ source .env
   $ set +a
   ```

4. `docker-compose.yml` 파일을 이용해 `Redis` 및 `MySQL` 도커 컨테이너를 실행합니다.(도커가 설치되어 있다고 가정합니다.)

   ```
   $ docker-compose up
   ```

5. java파일을 빌드, 실행합니다.

   ```sh
   $ ./gradlew clean build
   $ java -jar build/libs/server-0.0.1.jar
   ```

   - 실제 배포를 위한 `product` 환경일 시, `spring.profiles.active` 설정을 추가하여 실행합니다.

     ```sh
     $ ./gradlew clean build
     $ java -jar -Dspring.profiles.active=product build/libs/server-0.0.1.jar
     ```

## FEATURES

개발한 API들의 핵심 특성을 서술합니다.

> 각 api들의 상세한 명세는 [Swagger 문서](https://ka02fa9a0d9a2a.user-app.krampoline.com/api/docs/swagger)를 확인 부탁드립니다.

### 인증

JWT를 이용하였습니다.

- ACCESS TOKEN과 REFRESH TOKEN을 구현하였으며 REFRESH TOKEN은 REDIS에 저장됩니다.
- RTR(refresh token rotation) 전략으로 리프레시를 일회용으로 사용하였습니다.
- ACCESS 토큰은 AUTHORIZATION 헤더로 BEARER 토큰으로 전달합니다.
- REFRESH 토큰은 HTTP-ONLY 쿠키로 전달하여 클라이언트의 접근을 막아 보안성을 강화하였습니다.
- 회원가입 시, 유저 권한을 바로 부여하지 않고 이메일인증을 필요하게 하여 보안성, 안정성을 강화하였습니다.

**회원가입을 하지 않은 유저는 아래의 기능만 사용이 가능합니다.(주로 조회만 가능)**

    - 행정구역 관련 기능
    - 모집글 조회
    - 댓글 조회
    - 사용자 조회 (유저 기록 조회, 참여 기록 조회, 점수 조회)

**회원가입을 한 유저는 아래의 기능을 추가로 사용할 수 있습니다.**

    - 자신의 회원정보 조회 및 수정
    - 쪽지 조회

**회원 가입 후 이메일인증을 받은 유저는 아래의 기능을 포함하여 모든 기능을 사용할 수 있습니다.**

    - 모집글 관련 모든 기능
    - 신청 관련 모든 기능
    - 댓글 관련 모든 기능
    - 쪽지 관련 모든 기능
    - 별점 등록, 점수 등록 기능

### 행정 구역

정부의 행정구역 데이터를 db에 미리 작성해두어 행정구역 데이터를 직접 관리하도록 구현하였습니다.

> [행정표준코드관리 시스템 - 법정동코드 목록 조회](https://www.code.go.kr/stdcode/regCodeL.do) 데이터 파일을 이용하여 sql 데이터를 생성하였습니다.

### 모집글

볼링 번개 모임을 모집하려는 사람이 작성하는 포스팅입니다.

- 모집글을 등록하면, 타 유저들이 해당 모집글에 신청을 할 수 있고, 모임을 결성할 수 있습니다.
- 참가희망자들은 참가신청 기능을 이용하여 참가신청을 할 수 있고, 모집글 게시자가 참가수락 여부를 결정할 수 있습니다.
- 댓글 기능이 있어 정보 교환 및 소통이 가능합니다.
- 이후 모집글을 베이스로 참여기록, 별점, 점수등록 등이 이루어 지기 때문에모임 확정 이후에는 모집글 내용을 수정하거나 삭제할 수 없습니다.

#### JPA Specification

모집글 데이터가 필요한 API를 구현하는 도중, 복잡한 조건처리가 필요한 로직이 있어 동적 쿼리가 필요했습니다. `queryDSL`을 학습하여 도입하기에는 프로젝트 기간이 한정적이라 비교적 사용이 쉬운 JPA Specification을 사용하였습니다.

- 참여기록 데이터에 적용된 `JPA Specification`의 일부 로직입니다.

  ```java
  private List<Post> loadPosts(CursorRequest cursorRequest, Long userId, String condition, String status, Long cityId, String start, String end) {
      int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
      Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("id")));

      Specification<Post> spec = Specification.where(conditionEqual(condition, userId))
              .and(statusEqual(status))
              .and(cityIdEqual(cityId))
              .and(createdAtBetween(start, end));

      if (cursorRequest.hasKey()) {
          spec = spec.and(postIdLessThan(cursorRequest.key()));
      }
      return postRepository.findAll(spec, pageable).getContent();
  }
  ```

  > 해당 부분에서 동적쿼리를 사용하지 않으면 30개가 넘는 조건을 처리해야 했기 때문에 `JPA Specification`을 도입하게 되었습니다.

### 댓글

댓글에 대댓글을 달 수 있도록 설계하였습니다.

- 조회는 비회원도 가능하지만 작성은 회원만 가능합니다.
- 커서 기반 페이징을 하였습니다.
- 삭제된 댓글에 대댓글이 있는 경우 대댓글 표시를 위해 전달이 됩니다.

  - 삭제된 댓글이 표시될 경우 아래와 같이 표시됩니다.
    - `content: "삭제된 댓글입니다"`
    - `userId: null`

- 그외, 대댓글이 없는 삭제된 댓글과 대댓글이 삭제된 경우의 댓글은 responseBody에 포함되지 않습니다.

> 삭제된 댓글들로 인한 통일되지 않은 응답 개수 문제가 존재합니다.
>
> 이는 프론트의 구현이 무한 스크롤로 이루어 지기 때문에 적은 개수가 응답되어도 자동으로 다음 key로 요청이 일어나 사용자 경험에 큰 영향을 주지 않을 것으로 판단되어 이렇게 구현하였습니다.

### 신청, 별점

모집글 작성자 이외의 유저들은 모집글에 참여 신청을 할 수 있습니다.

- 작성자는 신청을 수락하거나 거부할 수 있습니다.
- 작성자는 신청자 목록, 신청수락완료 유저을 확인할 수 있습니다.
- 모임 시간 이후 사용자들은 별점 등록 API를 통해 참여자들에게 별점을 줄 수 있습니다.
- 별점은 사용자의 평점에 영향을 줍니다.

### 볼링 점수(스코어)

모임 이후, 해당 모임에 대해 점수를 등록할 수 있습니다.

- 점수와 함께 이미지를 등록 할 수 있어 점수에 대한 증명이 가능합니다.
- 모임 별 점수 등록은 여러개가 가능합니다.

#### 이미지 등록

외부 저장소 - `AWS S3`를 사용하였습니다.

- white listing 방식으로 확장자 검사가 이루어 집니다.
- 업로드 가능한 확장자는 png, jpg, jpeg, gif 4가지입니다.
- 이미지의 사이즈는 10MB로 제한하였습니다.

### 프로필, 정보

닉네임, 이메일, 지역 정보, 매너 점수, 볼링 Avergae 점수 등을 사용자 정보로 관리합니다.

- 타인의 프로필 조회 시 얻을 수 있는 정보
  - 닉네임
  - 매칭기록에 기반한 볼링 Average점수
  - 매너 점수
  - 지역
  - 프로필 사진
- 자신의 프로필 조회 시 추가로 얻을 수 있는 정보
  - id(PK)
  - 이메일
  - 메일인증여부

### 쪽지

다른 사용자와 1대1 대화를 할 수 있습니다.

- 카카오톡과 같은 채팅 서비스와 비슷한 사용자경험을 주기 위해 채팅 서비스와 유사하게 구현하였습니다.
- 웹소켓을 이용한 채팅 기능으로의 변경을 염두에 두고 구현하였습니다.

## Collaborators

[카카오 테크 캠퍼스](https://www.kakaotechcampus.com/) 1기 부산대 3조

### Backend

|                          조장                          |                        테크 리더                        |                        기획 리더                         |                       리액셔너                        |
| :----------------------------------------------------: | :-----------------------------------------------------: | :------------------------------------------------------: | :---------------------------------------------------: |
|          [박소현](https://github.com/sososo0)          |          [안혜준](https://github.com/jagaldol)          |          [김기해](https://github.com/xcelxlorx)          |          [김윤재](https://github.com/yunzae)          |
| <img src="https://github.com/sososo0.png" width="100"> | <img src="https://github.com/jagaldol.png" width="100"> | <img src="https://github.com/xcelxlorx.png" width="100"> | <img src="https://github.com/yunzae.png" width="100"> |

### Frontend

|                         리마인더                         |                          타임 키퍼                          |
| :------------------------------------------------------: | :---------------------------------------------------------: |
|          [강주호](https://github.com/kjh302903)          |          [허동혁](https://github.com/Heo-Donghyuk)          |
| <img src="https://github.com/kjh302903.png" width="100"> | <img src="https://github.com/Heo-Donghyuk.png" width="100"> |

<br><br><br><br><br>

<details>
    <summary><h2>카카오 테크 캠퍼스 3단계 진행 보드</h2></summary>
    </br>

## 배포와 관련하여

```

최종 배포는 크램폴린으로 배포해야 합니다.

하지만 배포 환경의 불편함이 있는 경우를 고려하여

임의의 배포를 위해 타 배포 환경을 자유롭게 이용해도 됩니다. (단, 금액적인 지원은 어렵습니다.)

아래는 추가적인 설정을 통해 (체험판, 혹은 프리 티어 등)무료로 클라우드 배포가 가능한 서비스입니다.

ex ) AWS(아마존), GCP(구글), Azure(마이크로소프트), Cloudtype

```

## Notice

```
필요 산출물들은 수료 기준에 영향을 주는 것은 아니지만,
주차 별 산출물을 기반으로 평가가 이루어 집니다.

주차 별 평가 점수는 추 후 최종 평가에 최종 합산 점수로 포함됩니다.
```

![레포지토리 운영-001 (1)](https://github.com/Step3-kakao-tech-campus/practice/assets/138656575/acb0dccd-0441-4200-999a-981865535d5f)
![image](https://github.com/Step3-kakao-tech-campus/practice/assets/138656575/b42cbc06-c5e7-4806-8477-63dfa8e807a0)

[git flowchart_FE.pdf](https://github.com/Step3-kakao-tech-campus/practice/files/12521045/git.flowchart_FE.pdf)

</br>

## 필요 산출물

<details>
<summary>Step3. Week-1</summary>
<div>
    
✅**1주차**
    
```
    - 5 Whys
    - 마켓 리서치
    - 페르소나 & 저니맵
    - 와이어 프레임
    - 칸반보드
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-2</summary>
<div>
    
✅**2주차**
    
```
    - ERD 설계서
    
    - API 명세서
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-3</summary>
<div>
    
✅**3주차**
    
```
    - 최종 기획안
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-4</summary>
<div>
    
✅**4주차**
    
```
    - 4주차 github
    
    - 4주차 노션
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-5</summary>
<div>
    
✅**5주차**
    
```
    - 5주차 github
    
    - 5주차 노션
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-6</summary>
<div>
    
✅**6주차**
    
```
    - 6주차 github
    
    - 중간발표자료
    
    - 피어리뷰시트
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-7</summary>
<div>
    
✅**7주차**
    
```
    - 7주차 github
    
    - 7주차 노션
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-8</summary>
<div>
    
✅**8주차**
    
```
    - 중간고사
    
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-9</summary>
<div>
    
✅**9주차**
    
```
    - 9주차 github
    
    - 9주차 노션
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-10</summary>
<div>
    
✅**10주차**
    
```
    - 10주차 github
    
    - 테스트 시나리오 명세서
    
    - 테스트 결과 보고서
```
    
</div>
</details>

---

<details>
<summary>Step3. Week-11</summary>
<div>
    
✅**11주차**
    
```
    - 최종 기획안
    
    - 배포 인스턴스 링크
```
    
</div>
</details>

---

## **과제 상세 : 수강생들이 과제를 진행할 때, 유념해야할 것**

```
1. README.md 파일은 동료 개발자에게 프로젝트에 쉽게 랜딩하도록 돕는 중요한 소통 수단입니다.
해당 프로젝트에 대해 아무런 지식이 없는 동료들에게 설명하는 것처럼 쉽고, 간결하게 작성해주세요.

2. 좋은 개발자는 디자이너, 기획자, 마케터 등 여러 포지션에 있는 분들과 소통을 잘합니다.
UI 컴포넌트의 명칭과 이를 구현하는 능력은 필수적인 커뮤니케이션 스킬이자 필요사항이니 어떤 상황에서 해당 컴포넌트를 사용하면 좋을지 고민하며 코드를 작성해보세요.

```

</br>

## **코드리뷰 관련: review branch로 PR시, 아래 내용을 포함하여 코멘트 남겨주세요.**

**1. PR 제목과 내용을 아래와 같이 작성 해주세요.**

PR 제목 : 부산대*0조*아이템명\_0주차

</br>

</div>

</details>
