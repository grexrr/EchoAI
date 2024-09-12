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


## Sept. 12 2024

### 1. Configuring Initial Authentication

Disable anti CSRF for the current stage. Configuring basic authentication mechanism as following: requiring all incoming http request to authenticate except for the ones matching pattern `/public/**`.

```JAVA
@Bean

SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	http
	.csrf(csrf -> csrf.disable()) // Disable CSRF (FOR NOW !!!!!!)
	.authorizeHttpRequests(auth -> auth // Configuring Authentication Rules
	.requestMatchers("/public/**").permitAll() // Public Paths
	.anyRequest().authenticated() // Others require authentication
	)
	.httpBasic(withDefaults()); // Default launching HTTP Basic authentication
	return http.build();

}
```

2 testing endpoints (public & non-public) past the test. JWT will be based upon this stage.
