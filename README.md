# Registration and Training Tracking System

Monolithic web application for managing Representatives, Delegators, Training Institutes, training requests, training delivery, and assessment results.

## Architecture

- One backend application
- One frontend application
- Modular monolith backend
- No microservices

## Stack

- Backend: Java 25 LTS, Spring Boot 4, Spring Security with JWT, Spring Data JPA, Hibernate, Flyway
- Frontend: React 19, Ant Design
- Database: PostgreSQL 18

## Business Rules

- A Representative can view trainings and request one.
- A Delegator approves or rejects requests based on pre-training requirements.
- A Training Institute creates trainings, manages enrolled trainees, and submits assessment scores.
- If a Representative passes, their status becomes TRAINED.
- After that, a Delegator can mark the Representative as AGENT.
- AGENT is only a Representative status, not a login role.

## Local Development

1. Start PostgreSQL.
2. Run the backend from `backend/` with Maven.
3. Run the frontend from `frontend/` with npm.

See the backend and frontend folders for implementation details.

## Validated Commands

- Backend test: `JAVA_HOME=%USERPROFILE%\.jdk\jdk-25` and `mvn test` from `backend/`
- Frontend install: `npm install` from `frontend/`
- Frontend build: `npm run build` from `frontend/`
