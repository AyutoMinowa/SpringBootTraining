package com.example.todolist.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {
	// JPQLによる検索
	List<Todo> findByJPQL(TodoQuery todoQuery);
	
	//Criteria APIによる検索
	//List<Todo> findByCriteria(TodoQuery todoQuery);
	
	Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable);
}

