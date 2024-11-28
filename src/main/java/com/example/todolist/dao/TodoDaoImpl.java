package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.entity.Todo_;
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
	private final EntityManager entityManager;

	// JPQLによる検索
	@Override
	public List<Todo> findByJPQL(TodoQuery todoQuery) {
		// ここを"todo"にすると実行時エラーになる。
		StringBuilder sb = new StringBuilder("select t from Todo t where 1 = 1");
		List<Object> params = new ArrayList<>();
		int pos = 0;
		// 実行するJPQLの組み立て
		// 件名
		if (todoQuery.getTitle().length() > 0) {
			sb.append(" and t.title like ?" + (++pos)); // ①
			params.add("%" + todoQuery.getTitle() + "%"); // ②
		}
		// 重要度
		if (todoQuery.getImportance() != -1) {
			sb.append(" and t.importance = ?" + (++pos)); // ①
			params.add(todoQuery.getImportance()); // ②
		}
		// 緊急度
		if (todoQuery.getUrgency() != -1) {
			sb.append(" and t.urgency = ?" + (++pos)); // ①
			params.add(todoQuery.getUrgency()); // ②
		}
		// 期限：開始～
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos)); // ①
			params.add(Utils.str2date(todoQuery.getDeadlineFrom())); // ②
		}
		// ～期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos)); // ①
			params.add(Utils.str2date(todoQuery.getDeadlineTo())); // ②
		}
		// 完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos)); // ①
			params.add(todoQuery.getDone()); // ②
		}
		// order
		sb.append(" order by id");
		Query query = entityManager.createQuery(sb.toString()); // ③
		for (int i = 0; i < params.size(); ++i) { // ④
			query = query.setParameter(i + 1, params.get(i));
		}
		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList(); // ⑤
		return list;
	}

	@Override
	public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
		Root<Todo> root = query.from(Todo.class);
		List<Predicate> predicates = new ArrayList<>();
		// 件名
		String title = "";
		if (todoQuery.getTitle().length() > 0) {
			title = "%" + todoQuery.getTitle() + "%";
		} else {
			title = "%";
		}
		predicates.add(builder.like(root.get(Todo_.TITLE), title));
		// 重要度
		if (todoQuery.getImportance() != -1) {
			predicates.add(
					builder.and(
							builder.equal(
									root.get(Todo_.IMPORTANCE), todoQuery.getImportance())));
		}
		// 緊急度
		if (todoQuery.getUrgency() != -1) {
			predicates.add(
					builder.and(
							builder.equal(
									root.get(Todo_.URGENCY), todoQuery.getUrgency())));
		}
		// 期限：開始～
		if (!todoQuery.getDeadlineFrom().equals("")) {
			predicates.add(
					builder.and(
							builder.greaterThanOrEqualTo(
									root.get(Todo_.DEADLINE),
									Utils.str2date(todoQuery.getDeadlineFrom()))));
		}
		// ～期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			predicates.add(
					builder.and(
							builder.lessThanOrEqualTo(
									root.get(Todo_.DEADLINE),
									Utils.str2date(todoQuery.getDeadlineTo()))));
		}
		// 完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			predicates.add(
					builder.and(
							builder.equal(
									root.get(Todo_.DONE), todoQuery.getDone())));
		}
		// SELECT作成
		Predicate[] predArray = new Predicate[predicates.size()];
		predicates.toArray(predArray);
		query = query
				.select(root).where(predArray).orderBy(builder.asc(root.get(Todo_.id)));
		// クエリ生成
		TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
		// 該当レコード数取得
		int totalRows = typedQuery.getResultList().size();
		// 先頭レコードの位置設定
		typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		// 1ページ当たりの件数
		typedQuery.setMaxResults(pageable.getPageSize());
		Page<Todo> page = new PageImpl<Todo>(typedQuery.getResultList(), pageable, totalRows);
		return page; // ⑥
	}
}
