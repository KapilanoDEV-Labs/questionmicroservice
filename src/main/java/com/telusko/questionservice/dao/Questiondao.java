package com.telusko.questionservice.dao;

import com.telusko.questionservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Questiondao extends JpaRepository<Question,Integer> {
    List<Question> findByCategory(String category);

    @Query(value = "SELECT q.id FROM question q WHERE q.category=:category ORDER BY RAND() LIMIT :numQ",nativeQuery = true)
    List<Integer> findRandomByCategory(String category, int numQ);
}
