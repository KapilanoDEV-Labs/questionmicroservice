# This is the question microservice

## Configuration

After I went to https://start.spring.io Maven's pom.xml would look like this.
```
<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>-->
<!--			<groupId>org.springframework.cloud</groupId>-->
<!--			<artifactId>spring-cloud-starter-openfeign</artifactId>-->
<!--		</dependency>
```

Initially I copied the controller, dao, models and services from the
monolithic service, then made sure the imports are correct.
### Database connectivity
I copied the application.properties across too as it contains datasource fields.



## Code

### Controller layer

In the monolithic app the quiz was responsible for generating questions.
It needed access to the question DB to do that. We don't want that as they
have separate DBs. The Question service will now generate them if the quiz
needs them. 
We need generate questions, getQuestions(questionid) and getScore to be
the responsibility of the Question service.