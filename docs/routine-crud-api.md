# 루틴 CRUD API

이 문서는 루틴 생성/조회/수정/삭제 API의 요청/응답 형식을 정리합니다.

## 공통 사항

- Base URL: `/api/routines`
- 날짜 포맷: `yyyy-MM-dd` (예: `2026-01-30`)
- 시간 포맷: `HH:mm` (예: `08:00`)
- 요일 표기: `daysOfWeek`는 `1~7` 범위의 정수 리스트 (1=월요일, 7=일요일)
- `isActive` 미지정 시 기본값은 `true`
- `isNotify`는 알림 여부를 의미하며, 미지정 시 `null`로 저장됩니다.
- `color`는 HEX 문자열을 사용합니다. (예: `#FF8800`)
- `title`은 공백만으로는 허용되지 않으며, 저장 시 양끝 공백이 제거됩니다.
- `daysOfWeek`는 중복 값이 있으면 제거되어 저장됩니다.
- `logDate` 포맷: `yyyy-MM-dd` (예: `2026-02-01`)
- 목록 조회에는 `logStatuses`가 포함됩니다. (해당 기간에 기록이 없으면 빈 배열)

## 루틴 생성

`POST /api/routines`

### Request Body

```json
{
  "userId": "google-uid",
  "title": "아침 운동",
  "goal": "스트레칭 10분",
  "color": "#FF8800",
  "time": "08:00",
  "startDate": "2026-02-01",
  "endDate": "2026-02-28",
  "isActive": true,
  "isNotify": true,
  "daysOfWeek": [1, 3, 5]
}
```

### Response (201 Created)

```json
{
  "id": 10,
  "userId": "google-uid",
  "title": "아침 운동",
  "goal": "스트레칭 10분",
  "color": "#FF8800",
  "time": "08:00",
  "startDate": "2026-02-01",
  "endDate": "2026-02-28",
  "isActive": true,
  "isNotify": true,
  "createdAt": "2026-01-30T09:00:00",
  "daysOfWeek": [1, 3, 5]
}
```

### 유효성/규칙

- `userId` 필수 (문자열)
- `title` 필수 (공백만 허용하지 않음)
- `time` 필수
- `startDate` 필수
- `endDate`는 `startDate` 이전 불가
- `daysOfWeek`는 `1~7` 범위만 허용, `null` 포함 불가

## 루틴 단건 조회

`GET /api/routines/{id}`

### Response (200 OK)

```json
{
  "id": 10,
  "userId": "google-uid",
  "title": "아침 운동",
  "goal": "스트레칭 10분",
  "color": "#FF8800",
  "time": "08:00",
  "startDate": "2026-02-01",
  "endDate": "2026-02-28",
  "isActive": true,
  "isNotify": true,
  "createdAt": "2026-01-30T09:00:00",
  "daysOfWeek": [1, 3, 5]
}
```

## 루틴 목록 조회

`GET /api/routines?userId={userId}&startDate={startDate}&endDate={endDate}`

### Query Params

- `userId` (필수)
- `startDate` (선택, `yyyy-MM-dd` 포맷). 미지정 시 서버 기준 오늘 날짜.
- `endDate` (선택, `yyyy-MM-dd` 포맷). 미지정 시 `startDate`와 동일.

### Response (200 OK)

```json
[
  {
    "id": 10,
    "userId": "google-uid",
    "title": "아침 운동",
    "goal": "스트레칭 10분",
    "color": "#FF8800",
    "time": "08:00",
    "startDate": "2026-02-01",
    "endDate": "2026-02-28",
    "isActive": true,
    "isNotify": true,
    "createdAt": "2026-01-30T09:00:00",
    "daysOfWeek": [1, 3, 5],
    "logStatuses": [
      {
        "id": 3,
        "routineId": 10,
        "logDate": "2026-02-01",
        "status": "DONE",
        "checkedAt": "2026-02-01T08:05:00"
      }
    ]
  },
  {
    "id": 11,
    "userId": "google-uid",
    "title": "저녁 독서",
    "goal": "소설 20쪽",
    "color": "#3355FF",
    "time": "18:00",
    "startDate": "2026-02-01",
    "endDate": null,
    "isActive": true,
    "isNotify": false,
    "createdAt": "2026-01-30T09:30:00",
    "daysOfWeek": [],
    "logStatuses": [
      {
        "id": 4,
        "routineId": 11,
        "logDate": "2026-02-01",
        "status": "MISS",
        "checkedAt": "2026-02-01T08:10:00"
      }
    ]
  }
]
```

## 루틴 수정

`PUT /api/routines/{id}`

### Request Body

```json
{
  "title": "아침 운동 - 변경",
  "goal": "스트레칭 15분",
  "color": "#00AA55",
  "time": "07:30",
  "startDate": "2026-02-01",
  "endDate": "2026-03-01",
  "isActive": false,
  "isNotify": false,
  "daysOfWeek": [2, 4, 6]
}
```

### Response (200 OK)

```json
{
  "id": 10,
  "userId": "google-uid",
  "title": "아침 운동 - 변경",
  "goal": "스트레칭 15분",
  "color": "#00AA55",
  "time": "07:30",
  "startDate": "2026-02-01",
  "endDate": "2026-03-01",
  "isActive": false,
  "isNotify": false,
  "createdAt": "2026-01-30T09:00:00",
  "daysOfWeek": [2, 4, 6]
}
```

### 유효성/규칙

- 요청 바디가 없으면 400
- `title`이 빈 문자열이면 400
- `endDate`는 `startDate` 이전 불가
- `daysOfWeek`가 포함되면 기존 요일을 전부 삭제 후 재등록
  - 빈 배열을 보내면 요일이 모두 삭제됨
  - 필드를 생략하면 기존 요일 유지

## AI 루틴 생성

`POST /api/routines/ai`

### Request Body

```json
{
  "userId": "google-uid",
  "prompt": "아침에 스트레칭하고 물 마시기",
  "intensity": "mid"
}
```

### Response (201 Created)

```json
[
  {
    "id": 12,
    "userId": "google-uid",
    "title": "아침에 스트레칭하고 물 마시기",
    "goal": "아침에 스트레칭하고 물 마시기",
    "color": null,
    "time": "00:00",
    "startDate": "2026-02-04",
    "endDate": null,
    "isActive": true,
    "isNotify": null,
    "createdAt": "2026-02-04T09:00:00",
    "daysOfWeek": []
  }
]
```

### 유효성/규칙

- `userId` 필수 (문자열)
- `prompt` 필수 (공백만 허용하지 않음)
- `intensity` 필수 (`high` | `mid` | `low`)

## 루틴 삭제

`DELETE /api/routines/{id}`

### Response (204 No Content)

응답 바디 없음

## 루틴 수행 토글

`POST /api/routines/{id}/logs/toggle`

### Request Body

```json
{
  "logDate": "2026-02-01"
}
```

### Response (200 OK) - 체크됨

```json
{
  "id": 3,
  "routineId": 10,
  "logDate": "2026-02-01",
  "status": "DONE",
  "checkedAt": "2026-02-01T08:05:00"
}
```

### Response (200 OK) - 체크 해제됨

```json
{
  "id": 3,
  "routineId": 10,
  "logDate": "2026-02-01",
  "status": "MISS",
  "checkedAt": "2026-02-01T08:10:00"
}
```

## 에러 응답

Spring Boot 기본 에러 포맷을 사용합니다. 예시는 다음과 같습니다.

```json
{
  "timestamp": "2026-01-30T09:10:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "title is required",
  "path": "/api/routines"
}
```

### 대표 에러 케이스

- 400 Bad Request
  - `request body is required`
  - `userId is required`
  - `title is required`
  - `title must not be blank`
  - `time is required`
  - `startDate is required`
  - `endDate must be on or after startDate`
  - `daysOfWeek contains null`
  - `dayOfWeek must be between 1 and 7`
- 404 Not Found
  - 루틴을 찾을 수 없습니다. `id={id}`
