# This is the question microservice
I copied from the monolithic application then made the necessary changes
to make this independent from the quiz service and have it's own DB.

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

### Service Layer

This code was initially taken from the monolithic QuizService.java file

List<Question> questionList = questiondao.findRandomByCategory(category,numQ);

We only need this because the DAO has a findRandomByCategory. Once copied, tidy
up the parameter names.

```
public ResponseEntity<List<Integer>> getQuestionsForQuiz(String categoryName, Integer numQuestions) {
        List<Integer> questionList = questiondao.findRandomByCategory(categoryName, numQuestions);

        return new ResponseEntity<>(questionList, HttpStatus.OK);
    }
```

Then I change the questiondao.findRandomByCategory so that instead of returning
a List of Question objects I return Integer since I am only returning the
question Ids.

For the next method if a quiz microservice is asking for a list of questions

```
    @PostMapping("getQuestions")
    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(@RequestBody List<Integer> questionIds) {
        System.out.println(environment.getProperty("local.server.port"));
        return questionService.getQuestionsFromId(questionIds);
    }
```

### DAO Layer
Since I am only returning the question Ids, instead of SELECT * I have q.id.

```
@Query(value = "SELECT q.id FROM question q WHERE q.category=:category ORDER BY RAND() LIMIT :numQ",nativeQuery = true)
    List<Integer> findRandomByCategory(String category, int numQ);
```

### Service Discovery

Using RestTemplate I can hit http://localhost:8080/question/generate Problem is
we are saying localhost. What if it is on another machine?
In mocroservices we are not sure where is the other instance so I don't want
to depend on the IP address. I don't want to hardcode the port number.
How would I know this?
We need a Feign client. It's like an HTTP client but instead you do not
need to hardcode anything.
It's a declarative way of finding the other services.
We also need a Service discovery. For the quiz service to find the question
service the latter needs to be discoverable. One such service is Eureka. All
of the MS need to register themselves to the Service registry on the Eureka Server.
Each MS needs a Eureka Client in order to do this.
