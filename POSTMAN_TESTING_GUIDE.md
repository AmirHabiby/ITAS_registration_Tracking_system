# Postman Testing Guide - ITAS Registration Tracking System

## Setup Instructions

### Base URL
```
http://localhost:8080
```

### Authentication Header
After login, add this header to all authenticated requests:
```
Authorization: Bearer <your_jwt_token>
```

### Content-Type Header
All requests should include:
```
Content-Type: application/json
```

---

## 1. AUTHENTICATION ENDPOINTS

### 1.1 POST /api/auth/login
**No Authentication Required**

```json
{
  "username": "admin",
  "password": "password"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresInSeconds": 3600,
  "username": "admin",
  "role": "SYSTEM_ADMIN",
  "displayName": "System Administrator"
}
```

---

### 1.2 POST /api/auth/logout
**Authentication Required: ANY**

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "message": "Logged out"
}
```

---

### 1.3 GET /api/auth/me
**Authentication Required: ANY**

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "admin",
  "role": "SYSTEM_ADMIN",
  "displayName": "System Administrator",
  "enabled": true
}
```

---

## 2. DASHBOARD ENDPOINTS

### 2.1 GET /api/dashboard/admin
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "totalUsers": 45,
  "totalTrainings": 12,
  "totalEnrollments": 156,
  "totalAssessments": 143,
  "recentActivity": [...]
}
```

---

### 2.2 GET /api/dashboard/delegator
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "totalRepresentatives": 25,
  "trainedRepresentatives": 18,
  "pendingRequests": 3,
  "totalRequests": 42
}
```

---

### 2.3 GET /api/dashboard/institute
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "totalTrainings": 8,
  "totalEnrollments": 156,
  "completedAssessments": 143,
  "pendingAssessments": 13
}
```

---

### 2.4 GET /api/dashboard/representative
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "totalTrainings": 5,
  "completedTrainings": 3,
  "passedTrainings": 2,
  "pendingResults": 1
}
```

---

### 2.5 GET /api/dashboard/me
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE, DELEGATOR, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

Returns appropriate dashboard based on user's role.

---

## 3. TRAINING ENDPOINTS

### 3.1 GET /api/trainings/available
**No Authentication Required**

**Response (200):**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "trainingInstituteProfileId": "456e7890-e89b-12d3-a456-426614174001",
    "title": "Basic IT Skills",
    "description": "Introduction to computer basics",
    "startDate": "2026-09-15",
    "endDate": "2026-09-30",
    "capacity": 30,
    "passingScore": 70.0,
    "allowedRetakeAttempts": 2,
    "status": "ACTIVE",
    "active": true
  }
]
```

---

### 3.2 GET /api/trainings/{id}
**No Authentication Required**

Replace `{id}` with training UUID.

**Response (200):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "trainingInstituteProfileId": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Basic IT Skills",
  "description": "Introduction to computer basics",
  "startDate": "2026-09-15",
  "endDate": "2026-09-30",
  "capacity": 30,
  "passingScore": 70.0,
  "allowedRetakeAttempts": 2,
  "status": "ACTIVE",
  "active": true
}
```

---

### 3.3 POST /api/trainings
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "instituteId": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Advanced Excel Skills",
  "description": "Learn advanced Excel formulas and pivot tables",
  "startDate": "2026-10-01",
  "endDate": "2026-10-15",
  "capacity": 25
}
```

**Response (201):**
```json
{
  "id": "789a1234-e89b-12d3-a456-426614174002",
  "trainingInstituteProfileId": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Advanced Excel Skills",
  "description": "Learn advanced Excel formulas and pivot tables",
  "startDate": "2026-10-01",
  "endDate": "2026-10-15",
  "capacity": 25,
  "passingScore": 0.0,
  "allowedRetakeAttempts": 0,
  "status": "PLANNED",
  "active": true
}
```

---

## 4. TRAINING INSTITUTE ENDPOINTS

### 4.1 GET /api/institutes
**No Authentication Required**

**Response (200):**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "name": "Tech Training Academy",
    "contactEmail": "info@techacademy.com",
    "active": true
  },
  {
    "id": "567f1234-e89b-12d3-a456-426614174002",
    "name": "Business Skills Institute",
    "contactEmail": "contact@bskills.com",
    "active": true
  }
]
```

---

### 4.2 GET /api/institutes/{id}
**No Authentication Required**

Replace `{id}` with institute UUID.

**Response (200):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "name": "Tech Training Academy",
  "contactEmail": "info@techacademy.com",
  "active": true
}
```

---

### 4.3 POST /api/institutes
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Digital Marketing Institute",
  "contactEmail": "contact@digitalmarketing.com"
}
```

**Response (201):**
```json
{
  "id": "678g2345-e89b-12d3-a456-426614174003",
  "name": "Digital Marketing Institute",
  "contactEmail": "contact@digitalmarketing.com",
  "active": true
}
```

---

## 5. TRAINING INSTITUTE PORTAL ENDPOINTS

### 5.1 POST /api/institutes/me/trainings
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "instituteId": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Data Analytics Basics",
  "description": "Learn data analytics using tools and spreadsheets",
  "startDate": "2026-10-20",
  "endDate": "2026-11-10",
  "capacity": 40
}
```

**Response (201):** Same as POST /api/trainings

---

### 5.2 GET /api/institutes/me/trainings
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "trainingInstituteProfileId": "456e7890-e89b-12d3-a456-426614174001",
    "title": "Basic IT Skills",
    "description": "Introduction to computer basics",
    "startDate": "2026-09-15",
    "endDate": "2026-09-30",
    "capacity": 30,
    "passingScore": 70.0,
    "allowedRetakeAttempts": 2,
    "status": "ACTIVE",
    "active": true
  }
]
```

---

### 5.3 PUT /api/institutes/me/trainings/{id}
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "instituteId": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Updated Training Title",
  "description": "Updated description",
  "startDate": "2026-10-20",
  "endDate": "2026-11-10",
  "capacity": 50
}
```

**Response (200):** Training object

---

### 5.4 DELETE /api/institutes/me/trainings/{id}
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (204):** No content

---

### 5.5 GET /api/institutes/me/enrollments
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "999h3456-e89b-12d3-a456-426614174004",
    "trainingId": "123e4567-e89b-12d3-a456-426614174000",
    "representativeId": "111i4567-e89b-12d3-a456-426614174005",
    "enrolledAt": "2026-09-10T10:30:00Z",
    "assessedAt": "2026-09-28T14:15:00Z",
    "score": 85.5,
    "status": "PASSED"
  }
]
```

---

## 6. ASSESSMENT ENDPOINTS

### 6.1 POST /api/assessments
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "enrollmentId": "999h3456-e89b-12d3-a456-426614174004",
  "score": 88.5,
  "remarks": "Excellent performance in practical tasks",
  "assessmentDate": "2026-09-28T14:15:00Z"
}
```

**Response (201):**
```json
{
  "id": "222j5678-e89b-12d3-a456-426614174006",
  "enrollmentId": "999h3456-e89b-12d3-a456-426614174004",
  "score": 88.5,
  "remarks": "Excellent performance in practical tasks",
  "assessmentDate": "2026-09-28T14:15:00Z"
}
```

---

### 6.2 GET /api/assessments
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "222j5678-e89b-12d3-a456-426614174006",
    "enrollmentId": "999h3456-e89b-12d3-a456-426614174004",
    "score": 88.5,
    "remarks": "Excellent performance in practical tasks",
    "assessmentDate": "2026-09-28T14:15:00Z"
  }
]
```

---

### 6.3 GET /api/assessments/{id}
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with assessment UUID.

**Response (200):** Single assessment object

---

## 7. ENROLLMENT ENDPOINTS

### 7.1 GET /api/enrollments
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "999h3456-e89b-12d3-a456-426614174004",
    "trainingId": "123e4567-e89b-12d3-a456-426614174000",
    "representativeId": "111i4567-e89b-12d3-a456-426614174005",
    "enrolledAt": "2026-09-10T10:30:00Z",
    "assessedAt": "2026-09-28T14:15:00Z",
    "score": 85.5,
    "status": "PASSED"
  }
]
```

---

### 7.2 POST /api/enrollments/assess
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "score": 92.0,
  "note": "Outstanding performance in all assessments"
}
```

**Response (201):** Enrollment object with assessment recorded

---

### 7.3 GET /api/enrollments/{id}
**Authentication Required:** SYSTEM_ADMIN, TRAINING_INSTITUTE

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with enrollment UUID.

**Response (200):** Single enrollment object

---

## 8. TRAINING REQUEST ENDPOINTS

### 8.1 GET /api/training-requests
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "333k6789-e89b-12d3-a456-426614174007",
    "representativeId": "111i4567-e89b-12d3-a456-426614174005",
    "trainingId": "123e4567-e89b-12d3-a456-426614174000",
    "requestedAt": "2026-09-05T09:00:00Z",
    "status": "PENDING",
    "delegatorRemarks": null
  }
]
```

---

### 8.2 POST /api/training-requests
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (201):**
```json
{
  "id": "333k6789-e89b-12d3-a456-426614174007",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "requestedAt": "2026-09-05T09:00:00Z",
  "status": "PENDING",
  "delegatorRemarks": null
}
```

---

### 8.3 PATCH /api/training-requests/{id}/approve
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "note": "Approved - Representative meets requirements"
}
```

**Response (200):**
```json
{
  "id": "333k6789-e89b-12d3-a456-426614174007",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "requestedAt": "2026-09-05T09:00:00Z",
  "status": "APPROVED",
  "delegatorRemarks": "Approved - Representative meets requirements"
}
```

---

### 8.4 PATCH /api/training-requests/{id}/reject
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "note": "Rejected - Training not aligned with current objectives"
}
```

**Response (200):**
```json
{
  "id": "333k6789-e89b-12d3-a456-426614174007",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "requestedAt": "2026-09-05T09:00:00Z",
  "status": "REJECTED",
  "delegatorRemarks": "Rejected - Training not aligned with current objectives"
}
```

---

### 8.5 GET /api/training-requests/{id}
**No Authentication Required**

Replace `{id}` with training request UUID.

**Response (200):** Single training request object

---

## 9. REPRESENTATIVE ENDPOINTS

### 9.1 GET /api/representatives
**No Authentication Required**

**Response (200):**
```json
[
  {
    "id": "111i4567-e89b-12d3-a456-426614174005",
    "userId": "444l7890-e89b-12d3-a456-426614174008",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "trained": false,
    "agent": false,
    "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
  }
]
```

---

### 9.2 POST /api/representatives
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com"
}
```

**Response (201):**
```json
{
  "id": "666n9012-e89b-12d3-a456-426614174010",
  "userId": null,
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "trained": false,
  "agent": false,
  "delegatorId": null
}
```

---

### 9.3 PATCH /api/representatives/{id}/trained
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with representative UUID.

**Response (200):**
```json
{
  "id": "111i4567-e89b-12d3-a456-426614174005",
  "userId": "444l7890-e89b-12d3-a456-426614174008",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "trained": true,
  "agent": false,
  "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
}
```

---

### 9.4 PATCH /api/representatives/{id}/agent
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with representative UUID.

**Response (200):**
```json
{
  "id": "111i4567-e89b-12d3-a456-426614174005",
  "userId": "444l7890-e89b-12d3-a456-426614174008",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "trained": true,
  "agent": true,
  "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
}
```

---

## 10. REPRESENTATIVE PORTAL ENDPOINTS

### 10.1 GET /api/representatives/me/trainings
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "trainingInstituteProfileId": "456e7890-e89b-12d3-a456-426614174001",
    "title": "Basic IT Skills",
    "description": "Introduction to computer basics",
    "startDate": "2026-09-15",
    "endDate": "2026-09-30",
    "capacity": 30,
    "passingScore": 70.0,
    "allowedRetakeAttempts": 2,
    "status": "ACTIVE",
    "active": true
  }
]
```

---

### 10.2 POST /api/representatives/me/training-requests
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (201):** Training request object

**Note:** System will use authenticated user's ID, not the one in request body

---

### 10.3 GET /api/representatives/me/training-requests
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "333k6789-e89b-12d3-a456-426614174007",
    "representativeId": "111i4567-e89b-12d3-a456-426614174005",
    "trainingId": "123e4567-e89b-12d3-a456-426614174000",
    "requestedAt": "2026-09-05T09:00:00Z",
    "status": "PENDING",
    "delegatorRemarks": null
  }
]
```

---

### 10.4 GET /api/representatives/me/results
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "777o3456-e89b-12d3-a456-426614174011",
    "trainingTitle": "Basic IT Skills",
    "enrolledAt": "2026-09-10T10:30:00Z",
    "assessedAt": "2026-09-28T14:15:00Z",
    "score": 85.5,
    "isPassed": true,
    "remarks": "Excellent performance"
  }
]
```

---

### 10.5 GET /api/representatives/me/dashboard
**Authentication Required:** SYSTEM_ADMIN, REPRESENTATIVE

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "totalTrainings": 5,
  "completedTrainings": 3,
  "passedTrainings": 2,
  "pendingResults": 1
}
```

---

## 11. DELEGATOR PORTAL ENDPOINTS

### 11.1 GET /api/delegators/me/training-requests
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "333k6789-e89b-12d3-a456-426614174007",
    "representativeId": "111i4567-e89b-12d3-a456-426614174005",
    "trainingId": "123e4567-e89b-12d3-a456-426614174000",
    "requestedAt": "2026-09-05T09:00:00Z",
    "status": "PENDING",
    "delegatorRemarks": null
  }
]
```

---

### 11.2 GET /api/delegators/me/training-requests/pending
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):** Array of pending training requests

---

### 11.3 PATCH /api/delegators/me/training-requests/{id}/approve
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body (Optional):**
```json
{
  "note": "Approved for training"
}
```

**Response (200):**
```json
{
  "id": "333k6789-e89b-12d3-a456-426614174007",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "requestedAt": "2026-09-05T09:00:00Z",
  "status": "APPROVED",
  "delegatorRemarks": "Approved for training"
}
```

---

### 11.4 PATCH /api/delegators/me/training-requests/{id}/reject
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "note": "Cannot approve at this time"
}
```

**Response (200):**
```json
{
  "id": "333k6789-e89b-12d3-a456-426614174007",
  "representativeId": "111i4567-e89b-12d3-a456-426614174005",
  "trainingId": "123e4567-e89b-12d3-a456-426614174000",
  "requestedAt": "2026-09-05T09:00:00Z",
  "status": "REJECTED",
  "delegatorRemarks": "Cannot approve at this time"
}
```

---

### 11.5 GET /api/delegators/me/trained-representatives
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "111i4567-e89b-12d3-a456-426614174005",
    "userId": "444l7890-e89b-12d3-a456-426614174008",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "trained": true,
    "agent": false,
    "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
  }
]
```

---

### 11.6 PATCH /api/delegators/me/representatives/{id}/mark-as-agent
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body (Optional):**
```json
{
  "note": "Marked as agent"
}
```

**Response (200):**
```json
{
  "id": "111i4567-e89b-12d3-a456-426614174005",
  "userId": "444l7890-e89b-12d3-a456-426614174008",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "trained": true,
  "agent": true,
  "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
}
```

---

### 11.7 GET /api/delegators/me/agents
**Authentication Required:** SYSTEM_ADMIN, DELEGATOR

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "111i4567-e89b-12d3-a456-426614174005",
    "userId": "444l7890-e89b-12d3-a456-426614174008",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "trained": true,
    "agent": true,
    "delegatorId": "555m8901-e89b-12d3-a456-426614174009"
  }
]
```

---

## 12. ADMIN ENDPOINTS

### 12.1 POST /api/admin/representatives
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "rep_john",
  "password": "SecurePassword123!",
  "displayName": "John Representative",
  "fullName": "John Michael Doe",
  "email": "john.rep@example.com",
  "enabled": true
}
```

**Response (201):**
```json
{
  "id": "444l7890-e89b-12d3-a456-426614174008",
  "username": "rep_john",
  "displayName": "John Representative",
  "role": "REPRESENTATIVE",
  "enabled": true
}
```

---

### 12.2 POST /api/admin/delegators
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "del_jane",
  "password": "SecurePassword123!",
  "displayName": "Jane Delegator",
  "fullName": "Jane Marie Smith",
  "email": "jane.delegator@example.com",
  "enabled": true
}
```

**Response (201):**
```json
{
  "id": "555m8901-e89b-12d3-a456-426614174009",
  "username": "del_jane",
  "displayName": "Jane Delegator",
  "role": "DELEGATOR",
  "enabled": true
}
```

---

### 12.3 POST /api/admin/training-institutes
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "inst_techacademy",
  "password": "SecurePassword123!",
  "displayName": "Tech Academy Admin",
  "name": "Tech Training Academy",
  "contactEmail": "admin@techacademy.com",
  "enabled": true
}
```

**Response (201):**
```json
{
  "id": "666n9012-e89b-12d3-a456-426614174010",
  "username": "inst_techacademy",
  "displayName": "Tech Academy Admin",
  "role": "TRAINING_INSTITUTE",
  "enabled": true
}
```

---

### 12.4 GET /api/admin/users
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
[
  {
    "id": "123a4567-e89b-12d3-a456-426614174000",
    "username": "admin",
    "displayName": "System Administrator",
    "role": "SYSTEM_ADMIN",
    "enabled": true
  },
  {
    "id": "444l7890-e89b-12d3-a456-426614174008",
    "username": "rep_john",
    "displayName": "John Representative",
    "role": "REPRESENTATIVE",
    "enabled": true
  }
]
```

---

### 12.5 PATCH /api/admin/users/{id}/activate
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with user UUID.

**Response (200):**
```json
{
  "id": "444l7890-e89b-12d3-a456-426614174008",
  "username": "rep_john",
  "displayName": "John Representative",
  "role": "REPRESENTATIVE",
  "enabled": true
}
```

---

### 12.6 PATCH /api/admin/users/{id}/deactivate
**Authentication Required:** SYSTEM_ADMIN

Headers:
```
Authorization: Bearer <jwt_token>
```

Replace `{id}` with user UUID.

**Response (200):**
```json
{
  "id": "444l7890-e89b-12d3-a456-426614174008",
  "username": "rep_john",
  "displayName": "John Representative",
  "role": "REPRESENTATIVE",
  "enabled": false
}
```

---

## Common Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input format",
  "path": "/api/trainings"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing JWT token",
  "path": "/api/dashboard/admin"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "User does not have permission to access this resource",
  "path": "/api/admin/users"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/trainings/invalid-uuid"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An internal error occurred",
  "path": "/api/trainings"
}
```

---

## Testing Tips

1. **Save Token**: After login, copy the token and add it to Postman's Authorization header for all authenticated requests
2. **Use Environment Variables**: Create Postman environment variables for:
   - `base_url`: http://localhost:8080
   - `token`: JWT token from login
   - `representative_id`: UUID of test representative
   - `training_id`: UUID of test training
3. **Test Order**:
   - First: Login to get token
   - Then: Create test data (institutes, trainings, users)
   - Finally: Test workflows (requests, approvals, assessments)
4. **Response Time**: Check the response time in Postman to identify slow endpoints
5. **Validation**: Check status codes match expected values (201 for creation, 200 for success, etc.)

---

## Sample Postman Collection Structure

```
ITAS Registration Tracking System
├── Authentication
│   ├── Login
│   ├── Logout
│   └── Get Current User
├── Dashboard
│   ├── Admin Dashboard
│   ├── Delegator Dashboard
│   ├── Institute Dashboard
│   └── Representative Dashboard
├── Trainings
│   ├── List Available
│   ├── Get Training Details
│   └── Create Training
├── Institutes
│   ├── List Institutes
│   ├── Get Institute Details
│   └── Create Institute
├── Assessments
│   ├── Submit Assessment
│   ├── List Assessments
│   └── Get Assessment Details
├── Training Requests
│   ├── List All Requests
│   ├── Create Request
│   ├── Approve Request
│   └── Reject Request
├── Representatives
│   ├── List Representatives
│   ├── Create Representative
│   ├── Mark as Trained
│   └── Mark as Agent
├── Delegator Portal
│   ├── List Pending Requests
│   ├── Approve/Reject Requests
│   └── Manage Agents
├── Representative Portal
│   ├── View Trainings
│   ├── Request Training
│   ├── View Results
│   └── View Dashboard
└── Admin
    ├── Create Users
    ├── List Users
    ├── Activate/Deactivate Users
    └── Manage Users
```
