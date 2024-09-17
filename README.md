# EchoAI


#Stage 1: Project Setup and Development Environment Preparation

***
- Establish a solid foundation for the project, ensuring all development tools and environments are properly configured.

## Highlights

- Use of **Docker** for containerization and automated management of the local development environment. Particularly, utilizing `docker-compose` to initiate MySQL and Spring Boot services, demonstrating an understanding of containerization and microservices.
# Steps

1. **GitHub Repository Creation**:
    - Create a new repository on GitHub for version control and to document project progress.
2. **Spring Boot Project Initialization**:
    - Use [Spring Initializr](https://start.spring.io/) to create a new Spring Boot project. Select Maven as the build tool and Java as the language, adding necessary dependencies such as Spring Web, Spring Security, Spring Data JPA, and MySQL Driver.
3. **Docker Environment Setup** (for local development and future deployment):
    - Docker is central to development and deployment. Initially configure MySQL locally using Docker, then containerize the Spring Boot application.
    - Write a `docker-compose.yml` file to manage multiple containers (MySQL and Spring Boot application), ensuring a seamless transition to AWS deployment in the future.
4. **Hello World Basic Application**:
    - Develop a simple Spring Boot application to verify that the development environment is functioning correctly and execute the application through a Docker container.

## Future AWS Deployment:

- Configure Docker and Docker Compose to simplify future deployment on AWS platforms such as EC2 or Elastic Beanstalk, ensuring consistency across environments.
# Log

## Sept. 8 2024
### Issue1: Initial test run

Attempt to access:

```JAVA
@RestController  
class HelloController {  
    @GetMapping("/hello")  
    public String hello() {  
       return "Hello EchoAI!";  
    }  
}
```

Received error:

```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
```

#### Reason & Solution

Spring Boot fail to configure **DataSource (DB)** and DB connection as default. Since no imbedded DB (like H2) had been configured by this stage, the issue occurred. By temporarily disabling **DataSourceAutoConfiguration.class** it allows access through localhost: 8080. However, Spring Security still requires a login. To bypass the blockage **SecurityAutoConfiguration.class** should also be disabled.

```JAVA
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class})
public class EchoAiApplication { 
    public static void main(String[] args){
	    SpringApplication.run(EchoAiApplication.class, args); 
	} 
}
```

###  Issue2: Testing Docker Image via Dockerfile

Encountered error:

```
-------------------- 
2 | FROM openjdk:17-jdk 
3 | WORKDIR /app 
4 | >>> COPY target/echoai-0.0.1-SNAPSHOT.jar app.jar 
5 | ENTRYPOINT ["java", "-jar", "app.jar"] 
6 | -------------------- 
ERROR: failed to solve: failed to compute cache key: failed to calculate checksum of ref 81j9wefaejxg98r3ci9xfg0yd::ts03xfqyqz4grxowgco3m1v50: "/target/echoai-0.0.1-SNAPSHOT.jar": not found
```


Docker could not found the destination **JAR** file ***simply*** because I did not generate one. By using: 

```
./mvnw clean package
```

lead to the following issue:

```
line 114: ./.mvn/wrapper/maven-wrapper.properties: No such file or directory cannot read distributionUrl property in ./.mvn/wrapper/maven-wrapper.properties
```
#### Reason & Solution

The Maven Wrapper is a script that allows different developers to work under the same Maven version. By running `./mvnw clean package`, it cleans the last build under /target and packages a new version of the JAR, allowing the Docker image to load it.

***

#Stage2. User Authentication System

***
- Develop a User Authentication system including registration, login, and dialogue management.

# Steps

1. **Database Integration**:
    - Database: Launch **PostgreSQL** in docker
    - Configure **Spring Data JPA** in Spring Boot to simplify interaction with db
    - Define Java class mapping to db tables
2. **User Authentication System**:
	- Configure Spring Security to define security strategy and permission
	- JWT (JSON Web Token): Use JWT for stateless authentication to facilitate integration with frontend and mobile application
	- Register and Login endpoints to implement JWT authentication
	- User info storage
# Log
## Sept. 10 2024

### 1. Configuring Spring Boot connection to DB

A docker image containing PostgreSQL is launched. 

In ***application.properties*** following changes are made allowing JAVA main to establish control over DB through port 5432:
```
#Setting DB connection

spring.datasource.url=jdbc:postgresql://localhost:5432/echoaidb
spring.datasource.username=echoai_user
spring.datasource.password=echoai_password
spring.datasource.driver-class-name=org.postgresql.Driver

#Setting JPA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 2. Testing User class

1. Declaring User class as an entity:
   
   ```JAVA
package com.echoai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //username
    //password
    //getter & setter
}
```

- **@Entity** parsed by JPA Hibernation. Its properties will be mapped to a table under SQL (ORM) allowing CRUD manipulation. 
- @**ID** will identify id as a primary key
- @**GeneratedValue** controls SQL's strategy to generate the key
   
2. UserRepository interface is used by JPA(Hibernate) to execute CRUD
3. UserController injects UserRepository to use its CRUD functions and map them to a particular URL as following:
   
```JAVA
@RestController
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	//@GetMapping("/addUser")
	//public void addUser(@RequestParam String username, 
	//					  @RequestParam String password){...}
	//...
}
```
#### Issue1: 

**Cannot load driver class: org.postgresql.Driver**
Because Maven needs to import a PostgreSQL driver which I did not know.
#### Reason & Solution

```XML
<dependency> 
	<groupId>org.postgresql</groupId> 
	<artifactId>postgresql</artifactId> 
	<version>42.7.4</version> 
</dependency>
```

42.7.4 was the latest version at the time of editing.

#### Issue2: 

Encountering...

```
Whitelabel Error Page 

This application has no explicit mapping for /error, so you are seeing this as a fallback. Tue Sep 10 05:18:09 IST 2024 There was an unexpected error (type=Internal Server Error, status=500).
```

...while testing /addUser.

#### Reason & Solution

"status-500" indicates a connection is established so the problem is within DB.

Terminal prompts...

```
ERROR: syntax error at or near "user" Position: 13 
```

Reason being "user" has been reserved by PostgreSQL. So rename the user table will be the solution by annotating User class with @Table(name = "<somename>")


## Sept. 16, 2024

### 1. Configure Jenkins Local Environment

#### Goal:
Implement a CI/CD pipeline using **Jenkins** to automate the management of Spring Boot applications and database Docker images.

#### Steps:

1. **Jenkins Installation**: Install Jenkins via Docker and run the Jenkins service in the local environment.
    ```bash
    docker run -p 9090:8080 -p 50000:50000 jenkins/jenkins:lts
    ```
2. **Pull Application Code**: Configure Jenkins to pull the application code from a GitHub repository for continuous integration testing.
3. **Docker Management**: Attempt to manage Docker images directly within the Jenkins container, using `docker-compose` to start the Spring Boot application and PostgreSQL database.

#### Issues1: 

While attempting to use a Jenkins container to manage local Docker images through the Docker socket, I encountered a startup sequence issue. The problem arises because:

- The Spring Boot application requires the PostgreSQL database to be up and running before it can start.
- However, the database is managed by docker-compose, and both services are tied together in the same docker-compose file, leading to a race condition where the application might attempt to start before the database is ready.
#### Reason & Solution

To resolve the startup sequence issue, I split the original docker-compose file into two distinct files:

1. The first docker-compose file is responsible for launching the PostgreSQL database container and ensuring it’s up and running.
```yml
services:
	db:
	image: postgres:13
	environment:
		POSTGRES_DB: echoaidb
		POSTGRES_USER: echoai_user
		POSTGRES_PASSWORD: echoai_password
	ports:	
		- "5432:5432"
	volumes:
		- postgres-data:/var/lib/postgresql/data
volumes:
	postgres-data:
```

2. The second docker-compose file will handle the application container startup, which only executes after the database has been successfully initialized and is available. Finally, the second `docker-compose-app.yml` **supposed to** start the application, using the depends_on directive to ensure that the database is ready before the application starts.
3. 
#### Issues2:
During the process, it was found that `docker-compose` and `docker compose` commands could not be used inside the Jenkins container because the local environment is ARM architecture (Apple M1/M2 chips), while the Jenkins container relies on x86 architecture Docker tools. This prevents Jenkins from directly starting Docker services and managing the CI/CD process within the container.

#### Solution Attempts:
- Tried exposing the local Docker environment to the Jenkins container via `Docker socket`, hoping that the Jenkins container could use the host's Docker service for build and deployment operations. However, due to architecture issues, the `docker-compose` command still could not run properly.

#### Logs:
During testing, it was found that Jenkins has compatibility issues running `docker-compose` on ARM architecture. It was ultimately confirmed that the Jenkins container could not use `docker-compose` and `docker compose` to manage application and database images due to ARM and x86 architecture compatibility issues.

---

## Solution Direction

### Use a More General System Architecture for CI/CD Automation

#### Feasibility of Kubernetes:

**Kubernetes** can serve as a more general architecture, especially suitable for automating the deployment and management of containerized applications. Compared to Docker Compose, Kubernetes offers more powerful container orchestration and management capabilities, making it suitable for cross-platform deployment needs. Kubernetes provides better scalability and cross-architecture support, allowing for consistency between local development and cloud deployment.

---

### Consideration of Dual-Environment vs. Kubernetes:

**Dual-Environment (Docker)**: This approach involves local development using Docker for testing while relying on an EC2-based Jenkins server for testing, building, and deployment in the cloud. Local Docker is ARM-based, while EC2 is x86-based.

- **Advantages**:
    
    - Easier to set up locally using Docker for fast, iterative development.
    - Lower initial learning curve since Docker Compose is simpler than Kubernetes.
- **Disadvantages**:
    
    - Managing large-scale, distributed systems with Docker Compose can be challenging.
    - Potential issues with differences in ARM (local) and x86 (EC2) environments, requiring multi-architecture Docker images.

**Kubernetes Solution**: This approach involves using **Minikube** or **K3s** locally to simulate a Kubernetes environment and deploying to a full Kubernetes cluster (e.g., EKS on AWS) in the cloud.

- **Advantages**:
    
    - **Unified development and production environments**: By using Kubernetes in both local and cloud environments, the development workflow is consistent, minimizing potential discrepancies between development and production.
    - **Scalability**: Kubernetes is better suited for handling large-scale, distributed applications with its built-in tools for auto-scaling, load balancing, and service discovery.
    - **Future-proofing**: As applications grow, Kubernetes provides more advanced features for managing complex architectures, including multi-cloud and hybrid deployments.
- **Disadvantages**:
    
    - **Higher initial complexity**: Kubernetes has a steeper learning curve than Docker Compose, especially for developers new to container orchestration.
    - **Resource-intensive**: Running Kubernetes locally or in the cloud requires more resources compared to Docker Compose.

---

### Decision:

The **Kubernetes solution** is the preferred option, as it provides a scalable, future-proof infrastructure for automating deployment and management of containerized applications. While the initial setup may be more complex, Kubernetes offers superior support for distributed systems and cross-platform deployment, ensuring consistency between local development and cloud production environments.
