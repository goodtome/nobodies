# Backend

## Start locally

1. Prepare a MySQL database named `nobodies`.
2. Fill environment variables based on `.env.example`.
3. Start the application with your IDE or Spring Boot command.

## Postman flow

### 1. Register

`POST /auth/register`

```json
{
  "email": "uploader@example.com",
  "password": "Password123"
}
```

### 2. Login

`POST /auth/login`

```json
{
  "email": "uploader@example.com",
  "password": "Password123"
}
```

Save the returned `accessToken` and send it as `Authorization: Bearer <token>`.

### 3. Init upload

`POST /videos/upload/init`

```json
{
  "fileName": "demo.mp4",
  "fileSize": 1048576,
  "fileType": "mp4",
  "chunkSize": 524288
}
```

### 4. Upload chunk(s)

`PUT /videos/upload/{uploadSessionId}/chunk`

Use `form-data`:
- `chunkNumber`: `1`
- `file`: choose the chunk file

Repeat for all parts. If you want a simple Postman demo, you can set `chunkSize == fileSize` in step 3 and upload the whole file as one chunk.

### 5. Query progress

`GET /videos/upload/{uploadSessionId}/progress`

### 6. Complete upload

`POST /videos/upload/{uploadSessionId}/complete`

```json
{
  "title": "My Demo Video"
}
```

### 7. Verify result

- `GET /videos`
- `GET /videos/{videoId}`

## Core modules

- `auth`: register, login, JWT token issuing
- `user`: admin user list and role updates
- `video`: video metadata, playback URL, soft delete
- `upload`: multipart upload session management and resumable progress tracking
- `storage`: Aliyun OSS integration wrapper

## Notes

- Files stay private in OSS and playback uses presigned URLs.
- For Postman debugging, one-file-one-chunk is the easiest path first.
- Real frontend integration can later replace manual Postman chunk submission with browser-side chunk splitting.
