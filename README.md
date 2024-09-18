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

---

### **Sept. 18, 2024**

## 1. **Establishment of 4 YAML Files**

**Objective:**  
Set up the deployment and services for both the Spring Boot application and the PostgreSQL database within Kubernetes. The goal was to create a functional setup where the application could communicate with the database through Kubernetes services.

**Steps:**
- Created 4 YAML files to manage the deployments and services for the application and the database:
  - **`postgresql_deployment.yaml`**: Defines the PostgreSQL database deployment.
  - **`postgresql_service.yaml`**: Defines the service for exposing PostgreSQL database.
  - **`deployment.yaml`**: Defines the Spring Boot application deployment.
  - **`service.yaml`**: Defines the service for exposing the Spring Boot application.
  
Each file had to be properly labeled and configured to ensure that the services correctly matched their respective Pods for communication within the Kubernetes cluster.

**Using k3d to bypass macOS Compatibility Issues:**
- **Docker Desktop**: Ensure Docker Desktop is installed and running on macOS, as k3d relies on Docker to create and manage Kubernetes clusters.
- **Resource Allocation**: Adjust the resource allocation (CPU and Memory) in Docker Desktop settings to ensure the Kubernetes cluster has enough resources to run both the Spring Boot application and the PostgreSQL database.
- **Networking**: macOS may have specific networking configurations that need to be addressed. Ensure that the Kubernetes services are correctly exposed and accessible from your local machine.
- **File System Permissions**: macOS has stricter file system permissions. Ensure that any volume mounts or file accesses within the Kubernetes cluster have the correct permissions set.

Each file had to be properly labeled and configured to ensure that the services correctly matched their respective Pods for communication within the Kubernetes cluster.

---

## 2. **Troubleshooting Application-Database Communication**

**Problem:**  
Initially, the Spring Boot application was unable to communicate with the PostgreSQL database due to incorrect configuration in the `application.properties` file. The application was trying to connect to the database using `localhost`, which doesn't work in a Kubernetes environment since the database is running in a different Pod.

**Solution:**
- Updated **`application.properties`** to use the PostgreSQL service name (`postgres-service`) instead of `localhost`, ensuring Kubernetes DNS resolves the correct service name. The modified connection string was as follows:

  ```yml
  spring.datasource.url=jdbc:postgresql://postgres-service:5432/echoaidb
  spring.datasource.username=echoai_user
  spring.datasource.password=echoai_password
  ```

- This allowed the application to correctly communicate with the PostgreSQL service, using the Kubernetes internal DNS for service discovery.

**Result:**  
The application and database were now properly connected, and internal communication between the two Pods was established successfully.

---

## 3. **Troubleshooting Label Mismatch in Services and Deployments**

**Problem:**  
Despite correcting the connection string, the Pods still couldn't communicate due to label mismatch issues between the deployments and services. This caused Kubernetes to not properly associate services with their respective Pods.

**Steps to Resolve:**
- **Key Diagnostic Command:** 
  We used the following commands to pinpoint the label issue:
  
  ```bash
  kubectl describe svc postgres-service
  kubectl describe svc echoai-app-service
  ```

- **Findings:**  
  The issue was that the labels in the services and deployments were inconsistent. The service selectors needed to match the labels defined in the deployments for correct traffic routing. Both `postgres-service` and `echoai-app-service` were unable to correctly associate with their respective Pods due to mismatched labels.

**Solution:**
- We standardized the labels across both the application and database deployment YAML files to ensure consistency. For example, the following label was applied uniformly:
  
  ```yaml
  app: echoai-app  # This label now matches in both the service and deployment.
  ```

**Result:**  
After applying the corrected labels, both services (`postgres-service` and `echoai-app-service`) correctly pointed to their respective Pods, allowing communication between the application and the database to work as intended.

---

## 4. **Remaining Issue: Timeout on Browser Access**

**Problem:**  
After successfully launching both the application and database Pods, and confirming their internal communication within the Kubernetes cluster, accessing the application externally via the browser still results in a timeout.

```bash
curl http://10.43.250.79:8080/public/hello
```
This indicates that while the application is correctly running, there is still an issue with external access to the application via the `LoadBalancer`.

**Next Steps for Resolution:**
1. **Check Traefik Configuration:**  
   Ensure Traefik is properly configured as the load balancer and is routing traffic to the `echoai-app-service`.
   
2. **Test ClusterIP Connectivity:**  
   From within the cluster, use the `ClusterIP` of `echoai-app-service` to test whether the service can be reached internally:
   
   ```bash
   curl http://10.43.250.79:8080/public/hello
   ```

3. **Inspect Service and Network Configuration:**  
   - Investigate if there are any `NetworkPolicy` or firewall settings that could be blocking external traffic.
   - Ensure the correct ports are open and that the `LoadBalancer` is configured correctly to forward external requests to the application.

---

### **Current Status:**
- The application and database Pods are running, and internal communication between them is working correctly.
- External access to the application is not yet functional, as attempts to access the application through the browser result in a timeout.
  
### **Next Steps:**
- Diagnose and fix the external access issue by reviewing Traefik, service configurations, and network policies.