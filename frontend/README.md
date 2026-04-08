# 前端

## Stack

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Element Plus

## Main pages

- `/login`
- `/register`
- `/dashboard`
- `/upload`
- `/videos`
- `/videos/:id`

## Local start

1. Make sure the backend is running on `http://localhost:8080` or update `.env.development`.
2. Install dependencies with `npm install`.
3. Start with `npm run dev`.

## Current flow

- Login and registration call the Spring Boot `/auth` endpoints.
- Upload page calls `/videos/upload/init`, then uploads each chunk to `/videos/upload/{id}/chunk`, then calls `/complete`.
- Videos page loads `/videos`.
- Video detail page loads `/videos/{id}` and plays the presigned URL.
