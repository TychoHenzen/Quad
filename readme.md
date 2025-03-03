# Quad Trivia Challenge

A Spring Boot application that provides a trivia quiz game using questions from the Open Trivia Database API.

## Project Overview

This application delivers a trivia quiz experience where users can:

- Select the number of questions they want to answer
- Answer multiple-choice questions from various categories and difficulty levels
- Get immediate feedback on their answers
- View their final score and correct answers
- Hides the correct answers provided by Open Trivia Database from end-users.

## Tech Stack

- Java 17
- Spring Boot 3.4.3
- Thymeleaf templating engine
- Bootstrap 5 for UI components
- JUnit 5 for testing
- JaCoCo for test coverage
- PIT for mutation testing

## Project Structure

The application follows a clean architecture approach with:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain the core business logic
- **DTOs**: Transfer data between layers
- **Exception handling**: Custom exceptions for clear error management
- **Unit and integration tests**: Comprehensive test coverage
- **Mutation testing**: mutation testing to ensure tests efficiency

## Building and Running the Application

### Prerequisites

- JDK 17
- Gradle (or use the included Gradle wrapper)
- Internet connection (to fetch trivia questions)

### Building

1. Clone the repository:
   ```bash
   git clone https://github.com/TychoHenzen/Quad.git
   cd Quad
   ```

2. Build with Gradle:
   ```bash
   ./gradlew build
   ```

### Running Locally

Run the application using:

```bash
./gradlew bootRun
```

The application will be available at http://localhost:8080

## Azure Deployment

The application has been deployed to Azure App Service using GitHub Actions for CI/CD.
The deployment process was set up using the Azure Web App portal:

1. Created a Web App in the Azure Portal:
    - Selected Java 17 as the runtime stack
    - Chose Java SE as the Java web server stack (since Spring Boot includes an embedded server)
    - Selected an appropriate App Service Plan

2. Connected the Azure Web App to GitHub:
    - Used Azure's Deployment Center to connect to the GitHub repository
    - Azure automatically generated the GitHub Actions workflow file

3. The GitHub Actions workflow (`.github/workflows/Azure-deploy.yml`) handles:
    - Building the application when code is pushed to the master branch
    - Deploying the built JAR to the Azure Web App

The deployed application is available at: https://quad-trivia-hvh4fjh7e8ezf3fw.northeurope-01.azurewebsites.net

## Testing

Run the tests with:

```bash
./gradlew test
```

Run mutation testing with:

```bash
./gradlew pitest
```

The project has extensive test coverage with:

- Unit tests for individual components
- Integration tests for component interactions
- End-to-end tests for complete application workflows
- mutation testing to ensure tests actually catch potential bugs

## API Endpoints

- `GET /`: Home page
- `GET /play`: Play the trivia game
- `GET /questions`: REST endpoint for trivia questions without revealing the correct answer
- `POST /checkanswers`: REST endpoint for checking answers