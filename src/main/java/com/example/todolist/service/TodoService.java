package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.form.TodoData;

@Service
public class TodoService {

    public boolean isValid(TodoData todoData, BindingResult result) {
        boolean ans = true;

        // 件名が全角スペースだけで構成されていたらエラー
        String title = todoData.getTitle();
        if (title != null && !title.equals("")) {
            boolean isAllDoubleSpace = true;
            for (int i = 0; i < title.length(); i++) {
                if (title.charAt(i) != '　') {
                    isAllDoubleSpace = false;
                    break;
                }
            }
            if (isAllDoubleSpace) {
                FieldError fieldError = new FieldError(
                    result.getObjectName(),
                    "title",
                    "件名が全角スペースです"
                );
                result.addError(fieldError);
                ans = false;
            }
        }

        // 期限が過去日付ならエラー
        String deadline = todoData.getDeadline();
        if (!deadline.equals("")) {
            LocalDate today = LocalDate.now();
            LocalDate deadlineDate = null;
            try {
                deadlineDate = LocalDate.parse(deadline);
                if (deadlineDate.isBefore(today)) {
                    FieldError fieldError = new FieldError(
                        result.getObjectName(),
                        "deadline",
                        "期限を設定するときは今日以降にしてください"
                    );
                    result.addError(fieldError);
                    ans = false;
                }
            } catch (DateTimeException e) {
                FieldError fieldError = new FieldError(
                    result.getObjectName(),
                    "deadline",
                    "期限を設定するときはyyyy-mm-dd形式で入力してください"
                );
                result.addError(fieldError);
                ans = false;
            }
        }

        return ans;
    }
}
