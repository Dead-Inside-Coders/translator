package com.tstu.backend.model;

public class Operation {
    private Keyword sign;
    private int priority;

    public Operation(Keyword sign, int priority) {
        this.sign = sign;
        this.priority = priority;
    }

    public Keyword getSign() {
        return sign;
    }

    public int getPriority() {
        return priority;
    }
}
