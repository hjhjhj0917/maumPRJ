# MAUM Project API Documentation

## 1. Authentication (`/api/v1/login`)

### 1.1. 로그인 및 토큰 발급
- **Endpoint:** `POST /api/v1/login/loginProc`
- **Description:** 사용자 아이디와 비밀번호로 로그인을 시도하고, 성공 시 Access Token과 Refresh Token을 쿠키로 발급합니다.
- **Request Body:**
  ```json
  {
    "userId": "testuser",
    "password": "password123"
  }
  ```
- **Response:**
  - **Success (200 OK):** `CommonResponse<MsgDTO>`
    - Access Token과 Refresh Token이 `HttpOnly` 쿠키로 설정됩니다.
  - **Failure (401 Unauthorized):** 인증 실패

### 1.2. 로그인 정보 확인
- **Endpoint:** `POST /api/v1/login/loginInfo`
- **Description:** 현재 유효한 JWT 토큰을 기반으로 로그인된 사용자의 정보를 반환합니다.
- **Request:** `Authorization` 헤더 또는 쿠키에 Access Token 필요.
- **Response:**
  - **Success (200 OK):** `CommonResponse<UserInfoDTO>`
    ```json
    {
      "userId": "testuser",
      "userName": "테스트유저",
      "roles": "USER"
    }
    ```

## 2. User Registration (`/api/v1/reg`)

### 2.1. 아이디 중복 확인
- **Endpoint:** `POST /api/v1/reg/getUserIdExists`
- **Description:** 회원가입 시 사용자 아이디의 중복 여부를 확인합니다.
- **Request Body:**
  ```json
  {
    "userId": "newuser"
  }
  ```
- **Response (200 OK):** `CommonResponse<UserInfoDTO>`
  - `existsYn` 필드가 "Y" 또는 "N"으로 반환됩니다.

### 2.2. 회원가입
- **Endpoint:** `POST /api/v1/reg/insertUserInfo`
- **Description:** 새로운 사용자를 등록합니다.
- **Request Body:** `UserInfoDTO` (모든 회원가입 필드 포함)
- **Response (200 OK):** `CommonResponse<MsgDTO>`
  - `result` 필드가 1이면 성공, 2이면 아이디 중복, 0이면 실패.

## 3. User Account (`/api/v1/account`)

### 3.1. 내 정보 조회
- **Endpoint:** `POST /api/v1/account/userInfo`
- **Description:** 현재 로그인된 사용자의 상세 정보를 조회합니다.
- **Request:** `Authorization` 헤더 또는 쿠키에 Access Token 필요.
- **Response (200 OK):** `CommonResponse<UserInfoDTO>`

### 3.2. 이메일 중복 확인 및 인증번호 발송
- **Endpoint:** `POST /api/v1/account/getEmailExists`
- **Description:** 이메일 중복 여부를 확인하고, 사용 가능하면 인증번호를 발송합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email` 파라미터
- **Response (200 OK):** `ExistsDTO`

### 3.3. 이메일 인증번호 확인
- **Endpoint:** `POST /api/v1/account/verifyEmailCode`
- **Description:** 발송된 이메일 인증번호를 확인합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email`, `code` 파라미터
- **Response (200 OK):** `MsgDTO`

### 3.4. 로그아웃
- **Endpoint:** `POST /api/v1/account/logout`
- **Description:** 사용자를 로그아웃 처리합니다. Access Token을 블랙리스트에 등록하고, 클라이언트의 토큰 쿠키를 삭제합니다.
- **Request:** `Authorization` 헤더 또는 쿠키에 Access Token 필요.
- **Response (200 OK):** `CommonResponse<MsgDTO>`

### 3.5. 아이디 찾기
- **Endpoint:** `POST /api/v1/account/findUserId`
- **Description:** 이메일과 이름으로 아이디 찾기를 요청하고, 인증번호를 발송합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email`, `userName` 파라미터
- **Response (200 OK):** `ExistsDTO`

### 3.6. 아이디 조회 (인증 후)
- **Endpoint:** `POST /api/v1/account/getUserId`
- **Description:** 인증번호 확인 후 실제 아이디를 조회합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email`, `userName`, `code` 파라미터
- **Response (200 OK):** `UserInfoDTO`

### 3.7. 비밀번호 찾기
- **Endpoint:** `POST /api/v1/account/findUserPw`
- **Description:** 아이디와 이메일로 비밀번호 찾기를 요청하고, 인증번호를 발송합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email`, `userId` 파라미터
- **Response (200 OK):** `ExistsDTO`

### 3.8. 비밀번호 수정
- **Endpoint:** `POST /api/v1/account/updateUserPw`
- **Description:** 인증번호 확인 후 비밀번호를 새로 설정합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `email`, `password`, `code` 파라미터
- **Response (200 OK):** `MsgDTO`

### 3.9. 프로필 이미지 수정
- **Endpoint:** `POST /api/v1/account/updateProfileImg`
- **Description:** 사용자의 프로필 이미지를 수정합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `profileImage` 파라미터
- **Response (200 OK):** `MsgDTO`

## 4. Diary (`/api/v1/diary`)

### 4.1. 일기 저장
- **Endpoint:** `POST /api/v1/diary/diaryInsert`
- **Description:** 새로운 일기를 작성하고 저장합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Body:** `title`, `content`, `createdAt` 파라미터
- **Response (200 OK):** `MsgDTO`

### 4.2. 월별 일기 목록 조회
- **Endpoint:** `GET /api/v1/diary/monthly`
- **Description:** 특정 년월의 일기 목록을 조회합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Parameters:** `createdAt` (e.g., "2024-05")
- **Response (200 OK):** `CommonResponse<List<DiaryDTO>>`

### 4.3. 일기 상세 조회
- **Endpoint:** `GET /api/v1/diary/detail`
- **Description:** 특정 일기의 상세 내용을 조회합니다. (세션 기반 로직, JWT 전환 후 수정 필요)
- **Request Parameters:** `diaryNo`
- **Response (200 OK):** `CommonResponse<DiaryDTO>`

## 5. Clova AI Analysis (`/api/clova`)

### 5.1. 텍스트 감정 분석
- **Endpoint:** `POST /api/clova/analyze`
- **Description:** 입력된 텍스트를 Clova Studio API를 통해 감정 분석합니다.
- **Request Body:**
  ```json
  {
    "content": "오늘 하루는 정말 즐거웠다."
  }
  ```
- **Response (200 OK):** `Map<String, Object>` (분석 결과)

---
**참고:** `UserInfoController`와 `DiaryController`의 일부 API는 여전히 `HttpSession`을 사용하고 있어 JWT 인증 방식으로의 완전한 전환을 위해 수정이 필요합니다.
