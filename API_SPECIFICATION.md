# Profile Server API Specification

## Base Information

- **Base URL**: `http://localhost:8080`
- **Version**: v1
- **Base Path**: `/api/v1/profiles`

---

## Table of Contents

1. [Health Check](#health-check)
2. [Enum APIs](#enum-apis)
3. [Profile Search APIs](#profile-search-apis)
4. [Profile Update APIs](#profile-update-apis)
5. [Error Responses](#error-responses)

---

## Health Check

### Check Server Status

서버 상태를 확인합니다.

```
GET /health
```

#### Response

```
200 OK
Content-Type: text/plain

Server is Running
```

---

## Enum APIs

게이트웨이에서 프론트엔드로 전달할 장르, 악기, 지역 정보를 조회합니다.

### Get Genres

모든 장르 목록을 조회합니다.

```
GET /api/v1/profiles/genres
```

#### Response

```json
200 OK
Content-Type: application/json

{
  "1": "Rock",
  "2": "Jazz",
  "3": "Classical",
  ...
}
```

### Get Instruments

모든 악기 목록을 조회합니다.

```
GET /api/v1/profiles/instruments
```

#### Response

```json
200 OK
Content-Type: application/json

{
  "1": "Guitar",
  "2": "Piano",
  "3": "Drums",
  ...
}
```

### Get Locations

모든 지역 목록을 조회합니다.

```
GET /api/v1/profiles/locations
```

#### Response

```json
200 OK
Content-Type: application/json

{
  "seoul": "서울특별시",
  "busan": "부산광역시",
  "daegu": "대구광역시",
  ...
}
```

---

## Profile Search APIs

### Get Single Profile

특정 사용자의 프로필을 조회합니다.

```
GET /api/v1/profiles/{userId}
```

#### Path Parameters

| Name   | Type   | Required | Description |
|--------|--------|----------|-------------|
| userId | string | Yes      | 사용자 ID      |

#### Response

```json
200 OK
Content-Type: application/json

{
  "userId": "user123",
  "sex": "M",
  "profileImageUrl": "https://example.com/image.jpg",
  "genres": ["Rock", "Jazz"],
  "instruments": ["Guitar", "Piano"],
  "introduction": "안녕하세요!",
  "city": "서울특별시",
  "nickname": "뮤지션123",
  "isChattable": true,
  "isPublic": true
}
```

### Search Profiles with Filters

조건에 맞는 프로필 목록을 커서 기반 페이지네이션으로 조회합니다.

```
GET /api/v1/profiles
```

#### Query Parameters

| Name        | Type      | Required | Default | Description         |
|-------------|-----------|----------|---------|---------------------|
| city        | string    | No       | -       | 도시 이름 (예: "seoul")  |
| nickName    | string    | No       | -       | 닉네임 (부분 일치)         |
| genres      | integer[] | No       | -       | 장르 ID 목록 (예: 1,2,3) |
| instruments | integer[] | No       | -       | 악기 ID 목록 (예: 1,2,3) |
| sex         | char      | No       | -       | 성별 ('M' or 'F')     |
| cursor      | string    | No       | -       | 다음 페이지를 위한 커서 값     |
| size        | integer   | No       | 10      | 페이지 크기 (1-100)      |

#### Example Request

```
GET /api/v1/profiles?city=seoul&genres=1&genres=2&instruments=1&sex=M&size=20
```

#### Response

```json
200 OK
Content-Type: application/json

{
  "content": [
    {
      "userId": "user123",
      "sex": "M",
      "profileImageUrl": "https://example.com/image.jpg",
      "genres": ["Rock", "Jazz"],
      "instruments": ["Guitar"],
      "introduction": "안녕하세요!",
      "city": "서울특별시",
      "nickname": "뮤지션123",
      "isChattable": true,
      "isPublic": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "size": 20,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

### Batch Get Profiles

여러 사용자의 프로필을 한 번에 조회합니다. (게이트웨이 내부 통신용)

```
POST /api/v1/profiles/batch
```

#### Query Parameters

| Name   | Type    | Required | Default | Description |
|--------|---------|----------|---------|-------------|
| detail | boolean | No       | false   | 상세 정보 포함 여부 |

#### Request Body

```json
Content-Type: application/json

["user123", "user456", "user789"]
```

#### Response (detail=false)

```json
200 OK
Content-Type: application/json

[
  {
    "userId": "user123",
    "nickname": "뮤지션123",
    "profileImageUrl": "https://example.com/image.jpg"
  },
  {
    "userId": "user456",
    "nickname": "뮤지션456",
    "profileImageUrl": "https://example.com/image2.jpg"
  }
]
```

#### Response (detail=true)

```json
200 OK
Content-Type: application/json

[
  {
    "userId": "user123",
    "sex": "M",
    "profileImageUrl": "https://example.com/image.jpg",
    "genres": ["Rock", "Jazz"],
    "instruments": ["Guitar", "Piano"],
    "introduction": "안녕하세요!",
    "city": "서울특별시",
    "nickname": "뮤지션123",
    "isChattable": true,
    "isPublic": true
  }
]
```

---

## Profile Update APIs

### Update Profile

사용자 프로필을 업데이트합니다.

```
PUT /api/v1/profiles/{userId}
```

#### Path Parameters

| Name   | Type   | Required | Description |
|--------|--------|----------|-------------|
| userId | string | Yes      | 사용자 ID      |

#### Request Body

```json
Content-Type: application/json

{
  "nickname": "새로운닉네임",
  "city": "seoul",
  "introduction": "자기소개 내용입니다.",
  "chattable": true,
  "publicProfile": true,
  "sex": "M",
  "genres": [1, 2, 3],
  "instruments": [1, 2]
}
```

#### Request Body Fields

| Name          | Type      | Required | Validation           | Description     |
|---------------|-----------|----------|----------------------|-----------------|
| nickname      | string    | No       | @NickName            | 닉네임             |
| city          | string    | No       | @Location            | 도시 코드           |
| introduction  | string    | No       | -                    | 자기소개            |
| chattable     | boolean   | No       | -                    | 채팅 가능 여부        |
| publicProfile | boolean   | No       | -                    | 프로필 공개 여부       |
| sex           | char      | No       | -                    | 성별 ('M' or 'F') |
| genres        | integer[] | No       | @Attribute(GENRE)    | 장르 ID 목록        |
| instruments   | integer[] | No       | @Attribute(INTEREST) | 악기 ID 목록        |

#### Response

```json
200 OK
Content-Type: application/json

true
```

---

## Error Responses

모든 에러는 다음 형식으로 반환됩니다.

### Error Response Format

```json
{
  "timestamp": "2025-10-25T14:30:00",
  "status": 400,
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "path": "/api/v1/profiles/user123"
}
```

### Common Error Codes

| Status Code               | Description              |
|---------------------------|--------------------------|
| 400 Bad Request           | 잘못된 요청 파라미터 또는 유효성 검증 실패 |
| 404 Not Found             | 요청한 리소스를 찾을 수 없음         |
| 500 Internal Server Error | 서버 내부 오류                 |

### Validation Error Example

```json
400 Bad Request
Content-Type: application/json

{
  "timestamp": "2025-10-25T14:30:00",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "닉네임은 2-20자 사이여야 합니다.",
  "path": "/api/v1/profiles/user123"
}
```

---

## Gateway Integration Notes

### 1. Authentication

게이트웨이는 모든 요청에 대해 인증을 처리하고, Profile Server로 요청을 전달할 때 `userId`를 포함해야 합니다.

### 2. Rate Limiting

게이트웨이 레벨에서 Rate Limiting을 구현하는 것을 권장합니다.

### 3. Timeout Settings

- 일반 조회: 5초
- 배치 조회: 10초
- 업데이트: 5초

### 4. Retry Policy

- 5xx 에러: 최대 3회 재시도 (exponential backoff)
- 4xx 에러: 재시도 없음

### 5. Circuit Breaker

게이트웨이에서 Circuit Breaker 패턴을 구현하여 Profile Server 장애 시 빠른 실패 처리를 권장합니다.

### 6. Request/Response Examples for Gateway

#### 게이트웨이 → Profile Server (인증된 요청)

```
GET /api/v1/profiles/user123
Authorization: Bearer {gateway-internal-token}
X-User-Id: user123
```

#### Profile Server → 게이트웨이 (응답)

```json
200 OK
Content-Type: application/json

{
  "userId": "user123",
  "nickname": "뮤지션123",
  ...
}
```

---

## Data Types Reference

### UserResponse

```typescript
interface UserResponse {
  userId: string;
  sex: 'M' | 'F';
  profileImageUrl: string;
  genres: string[];
  instruments: string[];
  introduction: string;
  city: string;
  nickname: string;
  isChattable: boolean;
  isPublic: boolean;
}
```

### BatchUserSummaryResponse

```typescript
interface BatchUserSummaryResponse {
  userId: string;
  nickname: string;
  profileImageUrl: string;
}
```

### ProfileUpdateRequest

```typescript
interface ProfileUpdateRequest {
  nickname?: string;
  city?: string;
  introduction?: string;
  chattable?: boolean;
  publicProfile?: boolean;
  sex?: 'M' | 'F';
  genres?: number[];
  instruments?: number[];
}
```

### Slice Response (Cursor-based Pagination)

```typescript
interface Slice<T> {
  content: T[];
  pageable: Pageable;
  size: number;
  number: number;
  sort: Sort;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

---

## Changelog

### Version 1.0 (2025-10-25)

- Initial API specification
- Profile CRUD operations
- Enum APIs for genres, instruments, locations
- Batch profile retrieval
- Cursor-based pagination for profile search
