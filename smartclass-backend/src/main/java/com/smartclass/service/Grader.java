package com.smartclass.service;

import com.smartclass.common.Constants;
import com.smartclass.entity.Question;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * 自动判分组件:客观题按题型分别比对答案
 */
@Component
public class Grader {

    /**
     * 判断学生答案是否正确
     * 单选/判断:忽略前后空格后精确匹配
     * 多选:选项集合完全一致才算正确(顺序无关)
     * 填空:忽略首尾空格、忽略大小写,支持竖线分隔的多个可选答案
     */
    public boolean judge(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return false;
        }
        String standard = question.getAnswer() == null ? "" : question.getAnswer().trim();
        String mine = studentAnswer.trim();
        return switch (question.getType()) {
            case Constants.QUESTION_MULTI -> optionSet(standard).equals(optionSet(mine));
            case Constants.QUESTION_FILL -> fillMatch(standard, mine);
            default -> standard.equalsIgnoreCase(mine);
        };
    }

    private TreeSet<String> optionSet(String answer) {
        TreeSet<String> set = new TreeSet<>();
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '对' || c == '错') {
                set.add(String.valueOf(Character.toUpperCase(c)));
            }
        }
        return set;
    }

    private boolean fillMatch(String standard, String mine) {
        return Arrays.stream(standard.split("\\|"))
                .map(String::trim)
                .anyMatch(candidate -> candidate.equalsIgnoreCase(mine));
    }
}
