package com.tstu.backend;

import com.tstu.backend.exceptions.*;

public interface ISyntaxAnalyzer {
    boolean checkSyntax() throws SyntaxAnalyzeException, ExpressionAnalyzeException, ConditionAnalyzeException, LexicalAnalyzeException, WhileAnalyzeException, CaseAnalyzeException;
}
