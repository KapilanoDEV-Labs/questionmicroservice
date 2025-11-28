package com.telusko.questionservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String category;
    private String difficultyLevel;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    @Getter
    @Setter
    private String questionTitle;
    private String rightAnswer;

}
