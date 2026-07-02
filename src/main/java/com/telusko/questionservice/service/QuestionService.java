package com.telusko.questionservice.service;

import com.telusko.questionservice.dao.Questiondao;
import com.telusko.questionservice.model.Question;
import com.telusko.questionservice.dto.QuestionWrapper;
import com.telusko.questionservice.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    Questiondao questiondao;
    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questiondao.findAll(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            return new ResponseEntity<>(questiondao.findByCategory(category),HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> addQuestion(Question question) {
        questiondao.save(question);
        return new ResponseEntity<>( "success",HttpStatus.CREATED);
    }

    public ResponseEntity<List<Integer>> getQuestionsForQuiz(String categoryName, Integer numQuestions) {
        List<Integer> questionList = questiondao.findRandomByCategory(categoryName, numQuestions);

        return new ResponseEntity<>(questionList, HttpStatus.OK);
    }

    public String deleteQuestionById(Integer id) {

        Optional<Question> result = questiondao.findById(id);
        questiondao.deleteById(id);

        Question question = result.get();
        String questionToBeDeleted = question.getQuestionTitle();

        return "Deleted question:\n" + questionToBeDeleted;
    }

    public String updateQuestion(Integer qid, Question question) {
        Optional<Question> result = questiondao.findById(qid);
        if(!result.isEmpty()) {
            Question question1 = result.get();
            question1.setQuestionTitle(question.getQuestionTitle());
            question1.setCategory(question.getCategory());
            question1.setOption1(question.getOption1());
            question1.setOption2(question.getOption2());
            question1.setOption3(question.getOption3());
            question1.setOption4(question.getOption4());
            question1.setDifficultyLevel(question.getDifficultyLevel());
            question1.setRightAnswer(question.getRightAnswer());
            questiondao.save(question1);
            return "updated";
        }
        else {return "Question " + qid + " does not exist!";}
    }

    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(List<Integer> questionIds) {
        List<QuestionWrapper> questionWrapperList = new ArrayList<QuestionWrapper>();

        for (Integer questionId : questionIds) {
            Question curQn = questiondao.findById(questionId).get();
            QuestionWrapper qw = new QuestionWrapper(curQn.getId(),curQn.getQuestionTitle(),curQn.getOption1(),curQn.getOption2(),curQn.getOption3(),curQn.getOption4());
            questionWrapperList.add(qw);
        }

        return new ResponseEntity<>(questionWrapperList,HttpStatus.OK);
    }

    public ResponseEntity<Integer> getScore(List<Response> responses) {

        String answer;
        Integer respId;
        int points=0;
        for (Response response : responses) {
            respId = response.getId();
            Optional<Question> qn = questiondao.findById(respId);
            answer = qn.get().getRightAnswer();

            if (response.getResponse().equals(answer))
                points++;
        }
        return new ResponseEntity<>(points, HttpStatus.OK);

    }
}
