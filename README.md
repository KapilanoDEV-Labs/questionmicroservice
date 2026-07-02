<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [This is the question microservice](#this-is-the-question-microservice)
  - [Configuration](#configuration)
    - [Database connectivity](#database-connectivity)
  - [Code](#code)
    - [Controller layer](#controller-layer)
    - [Service Layer](#service-layer)
    - [DAO Layer](#dao-layer)
    - [Service Discovery](#service-discovery)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# This is the question microservice
I copied from the monolithic application then made the necessary changes
to make this independent from the quiz service and have it's own DB.
This is a MS running under https://github.com/KapilanoDEV-Labs/

I had to change the repo URL 

```text
amitkapila@Amits-iMac question-service % git remote -v
origin  https://github.com/KapilanoDEV/questionmicroservice.git (fetch)
origin  https://github.com/KapilanoDEV/questionmicroservice.git (push)
amitkapila@Amits-iMac question-service % 
amitkapila@Amits-iMac question-service % git remote set-url origin https://github.com/KapilanoDEV-Labs/questionmicroservice.git
amitkapila@Amits-iMac question-service % git remote -v                                                                         
origin  https://github.com/KapilanoDEV-Labs/questionmicroservice.git (fetch)
origin  https://github.com/KapilanoDEV-Labs/questionmicroservice.git (push)
amitkapila@Amits-iMac question-service %
```

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

By default, SQL database initialization is only performed when using an embedded in-memory database. To always initialize an SQL database, irrespective of its type, set `spring.sql.init.mode` to `always`.



## Code

### Controller layer

In the monolithic app the quiz was responsible for generating questions.
It needed access to the question DB to do that. We don't want that as they
have separate DBs. The Question service will now generate them if the quiz
needs them. 
We need generate questions, getQuestions(questionid) and getScore to be
the responsibility of the Question service.

## 🏗️ Architectural Transition: Breaking the Monolith

In our old monolithic design, the `Quiz` and `Question` domains were tightly coupled. They shared the same database instance and the same runtime environment. When transitioning to a Microservices Architecture, we split them into independent deployments: **Quiz Service** and **Question Service**, each managing its own isolated database.

This architectural shift directly required us to redesign how these components talk to each other. Below is the engineering reasoning behind the new methods introduced in the Question Microservice.

---

### 1. `getQuestionsForQuiz(String category, Integer numQuestions)`

#### 🎯 Purpose:
Returns a list of randomly selected question IDs matching a specific category and volume count.

#### 💡 Architectural Reasoning:
* **Database Isolation (Loose Coupling):** In a microservice ecosystem, the `Quiz Service` should **never** directly access the `Question` database tables. The Quiz Service only needs to know *what* quiz exists and *which* question IDs are inside it.
* **Separation of Concerns:** Deciding which questions fit a specific category and randomly picking them is purely a feature of the question repository. The Quiz Service simply calls this endpoint to offload the question selection logic entirely.

---

### 2. `getQuestionsFromId(List<Integer> questionIds)`

#### 🎯 Purpose:
Accepts a collection of unique question IDs as an input array and returns a payload of `QuestionWrapper` Data Transfer Objects (DTOs).

#### 💡 Architectural Reasoning:
* **Network Payload Optimization:** A `QuestionWrapper` excludes the `rightAnswer` field. When a user is actively taking a quiz, we cannot expose the correct answers in the HTTP network response payload (which would allow users to look at the answers via browser developer tools).
* **Data Hydration:** The Quiz Service stores only raw question IDs to remain lightweight. When a frontend client requests to load a quiz, the Quiz Service uses this endpoint to "hydrate" those IDs into actual readable text, options, and titles.

---

### 3. `getScore(List<Response> responses)`

#### 🎯 Purpose:
Accepts a list of user submissions (containing the question ID and the user's chosen answer) and calculates the total numeric score.

#### 💡 Architectural Reasoning:
* **Encapsulation of Secrets (Security):** The `rightAnswer` column lives strictly inside the Question Service's isolated database. To calculate a score, something has to compare the user's answer against the correct answer.
* **Zero Leakage:** Instead of forcing the Question Service to send the correct answers over the network to the Quiz Service (which leaks data boundaries), the Quiz Service sends the user's responses *to* the Question Service. The Question Service evaluates the score internally where the data is safe, and returns only a single safe number (e.g., `5/10`).

---

## 📈 Benefits of this Design

1. **True Independence:** The Quiz Service can now function completely without knowing how questions are stored, structured, or validated.
2. **Scalability:** If the question database grows to millions of items, the Question Service can be scaled up independently without affecting the performance of the quiz engine.
3. **Enhanced Security:** Correct answers never leave the boundary of the Question Service's secure database until grading is complete.

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
