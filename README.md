
# User Management Service

This application provides basic user management functionalities, including user creation, retrieval, update, and deletion. The following documentation outlines how to build, run, and test the application, along with some design considerations.

## Table of Contents

- [Overview](#overview)
- [Build](#build)
- [Run](#run)
- [Test](#test)
- [Design Considerations](#design-considerations)
- [API Reference](#api-reference)
- [Docker](#docker)

## Overview

The User Management Service allows you to manage user information through a set of RESTful APIs. These APIs facilitate the creation, retrieval, update, and deletion of user records.

## Build

To build the application, you will need [Java](https://www.oracle.com/java/technologies/javase-downloads.html) and [Maven](https://maven.apache.org/download.cgi) installed on your machine. Follow these steps:

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/Suvrakar/user-management-service.git
   ```

2. Navigate to the project directory:

   ```bash
   cd user-management-app
   ```

3. Build the application using Maven:

   ```bash
   mvn clean install
   ```

This will compile the source code, run tests, and package the application.

## Run

To run the application, you can use the following steps:

1. Navigate to the target directory:

   ```bash
   cd target
   ```

2. Run the application:

   ```bash
   java -jar user-management-app.jar
   ```

This will start the application, and it will be accessible at `http://localhost:8080`.

## Test

To run the tests for the application, use the following command:

```bash
mvn test
```

This will execute all the test cases and provide the test results.

## Design Considerations

The application follows a RESTful architecture for user management. Key design considerations include:

- **Exception Handling:** The application handles different exceptions such as `UserNotFoundException` and `InvalidUserDataException` to provide meaningful error responses.

- **Data Storage:** User data is stored in a relational database (MySQL)and cached in Redis for improved performance.

- **API Documentation:** The API is documented using a consistent and clear format for easy understanding.

- **Caching:** Redis is used for caching user data, reducing database queries for frequently accessed information.



## API Reference

### Save User

Create a new user by providing user information.

- **Request:**
  ```http
  POST /api/users
  ```
  | Parameter | Type   | Description                |
  | --------- | ------ | -------------------------- |
  | `name`    | string | **Required**. User's name  |
  | `email`   | string | **Required**. User's email |

- **Response:**
  - Success (HTTP Status Code: 201 Created)
    ```http
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com"
    }
    ```
  - Invalid User Data (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "Invalid user data"
    }
    ```
  - Invalid Email Address (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "Invalid email address"
    }
    ```
  - Internal Server Error (HTTP Status Code: 500 Internal Server Error)
    ```http
    {
      "message": "Error processing the request"
    }
    ```

### Get User by ID

Retrieve user details by providing the user ID.

- **Request:**
  ```http
  GET /api/users/{id}
  ```
  | Parameter | Type   | Description                  |
  | --------- | ------ | ---------------------------- |
  | `id`      | long   | **Required**. User's ID to fetch |

- **Response:**
  - Success (HTTP Status Code: 200 OK)
    ```http
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com"
    }
    ```
  - User Not Found (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "User not found with ID: {id}"
    }
    ```

### Update User

Update user details by providing the user ID and updated information.

- **Request:**
  ```http
  PUT /api/users/{id}
  ```
  | Parameter | Type   | Description                  |
  | --------- | ------ | ---------------------------- |
  | `id`      | long   | **Required**. User's ID       |
  | `name`    | string | Updated user name            |
  | `email`   | string | Updated user email           |

- **Response:**
  - Success (HTTP Status Code: 200 OK)
    ```http
    {
      "id": 1,
      "name": "Updated Name",
      "email": "updated.email@example.com"
    }
    ```
  - User Not Found (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "User not found with ID: {id}"
    }
    ```
  - Invalid User Data (HTTP Status Code: 400 Bad Request)
    ```http
    {
      "message": "User name and email cannot be null"
    }
  - Invalid Email Address (HTTP Status Code: 400 Bad Request)
    ```http
    {
      "message": "Invalid email address"
    }
  - Internal Server Error (HTTP Status Code: 500 Internal Server Error)
    ```http
    {
      "message": "Internal Server Error"
    }
    ```

### Delete User

Delete a user by providing the user ID.

- **Request:**
  ```http
  DELETE /api/users/{id}
  ```
  | Parameter | Type   | Description                  |
  | --------- | ------ | ---------------------------- |
  | `id`      | long   | **Required**. User's ID       |

- **Responses:**
  - Success (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "User deleted successfully with ID: {id}"
    }
  - User Not Found (HTTP Status Code: 200 OK)
    ```http
    {
      "message": "User not found with ID: {id}"
    }
