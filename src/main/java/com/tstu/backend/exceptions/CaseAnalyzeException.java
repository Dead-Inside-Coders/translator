package com.tstu.backend.exceptions;

public class CaseAnalyzeException extends Exception {
    public CaseAnalyzeException(String message) {
        super("Не удалось разобрать switch-case :" + message);
    }
}
