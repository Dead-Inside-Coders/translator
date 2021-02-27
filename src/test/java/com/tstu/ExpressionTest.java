package com.tstu;

import com.tstu.backend.ILexicalAnalyzer;
import com.tstu.backend.INameTable;
import com.tstu.backend.ISyntaxAnalyzer;
import com.tstu.backend.exceptions.*;
import com.tstu.backend.lexems.IdentifierTable;
import com.tstu.backend.lexems.LexicalAnalyzer;
import com.tstu.backend.model.Keyword;
import com.tstu.backend.syntax.SyntaxAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ExpressionTest {

    private ISyntaxAnalyzer syntaxAnalyzer;

    private void init(String data) throws LexicalAnalyzeException {
        ILexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        List<Keyword> lexems = lexicalAnalyzer.recognizeAllLexem(
                "Var a,b,c,res : Logical\n" +
                        "Begin\n" +
                        "a:=0\n" +
                        "b:=0\n" +
                        "c:=0\n" +
                        data +
                        "End\n"
        );
        INameTable nameTable = new IdentifierTable();
        nameTable.recognizeAllIdentifiers(lexems);
        syntaxAnalyzer = new SyntaxAnalyzer(lexems, nameTable);
    }

    @Test
    public void positiveTestAssign() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }


    @Test
    public void positiveTestOrOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a|b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestAndOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a&b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestAndOrOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a&b|c\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestNotOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=!a\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestNotOrOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=!a|b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestNotAndOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=!a&b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestNotAndNotNotOrOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=!a&!b|!c\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test
    public void positiveTestOrNotAndOperation() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a&!b|c\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestTwoOperationOrInARow() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a||\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestTwoOperationAndInARow() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a&&\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestTwoOperationOrInARowAndVariable() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a||b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestTwoOperationAndInARowAndVariable() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a&&b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    //TODO хз по логике правильно конечно , а по коду по идеии ошибка должна быть
    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestTwoNotOperationInARow() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=!!a\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestNothingAfterAssign() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:= \n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeTestUnknownVariable() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=z\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = LexicalAnalyzeException.class)
    public void negativeTestUnknownSymbols1() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a|b#c\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = LexicalAnalyzeException.class)
    public void negativeTestUnknownSymbols2() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:=a%b&c\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = LexicalAnalyzeException.class)
    public void negativeMissedСolonInAssign() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res=a&b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

    @Test(expected = ExpressionAnalyzeException.class)
    public void negativeMissedEqualSigInAssign() throws LexicalAnalyzeException, ExpressionAnalyzeException, SyntaxAnalyzeException, ConditionAnalyzeException, WhileAnalyzeException, CaseAnalyzeException {
        init("res:a&b\n");
        Assert.assertTrue(syntaxAnalyzer.checkSyntax());
    }

}
