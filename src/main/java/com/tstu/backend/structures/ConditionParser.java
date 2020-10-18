package com.tstu.backend.structures;

import com.tstu.backend.IConditionParser;
import com.tstu.backend.INameTable;
import com.tstu.backend.exceptions.ConditionAnalyzeException;
import com.tstu.backend.exceptions.ExpressionAnalyzeException;
import com.tstu.backend.exceptions.LexicalAnalyzeException;
import com.tstu.backend.generator.CodeGenerator;
import com.tstu.backend.model.Identifier;
import com.tstu.backend.model.Keyword;
import com.tstu.backend.model.enums.Command;
import com.tstu.backend.model.enums.Lexems;
import com.tstu.backend.model.enums.tCat;

import java.util.List;

public class ConditionParser implements IConditionParser {

    private List<List<Keyword>> conditionArea;
    private INameTable nameTable;
    private List<Identifier> declaratedVariable;

    public ConditionParser(List<List<Keyword>> conditionArea, List<Identifier> declaratedVariable, INameTable nameTable) {
        this.conditionArea = conditionArea;
        this.nameTable = nameTable;
        this.declaratedVariable = declaratedVariable;
    }

    @Override
    public boolean parseCondition() throws ConditionAnalyzeException, LexicalAnalyzeException, ExpressionAnalyzeException {

        ExpressionParser expressionParser;

        parseIfThen(conditionArea.get(0));
        CodeGenerator.addInstruction("if: ");

        List<Keyword> expression = conditionArea.get(1);
        if (!isExpression(expression)) {
            throw new ConditionAnalyzeException("Ожидается выражение");
        }
        expressionParser = new ExpressionParser(expression, declaratedVariable, nameTable);
        expressionParser.parseExpression();
        CodeGenerator.addInstruction("jmp cont");

        if (isElse()) {
            if (!conditionArea.get(2).get(0).word.equals(Command.ELSE.getName())) {
                throw new ConditionAnalyzeException("Ожидается команда ELSE");
            }

            CodeGenerator.addInstruction("else: ");

            expression = conditionArea.get(3);

            if (!isExpression(expression)) {
                throw new ConditionAnalyzeException("Ожидается выражение");
            }

            expressionParser = new ExpressionParser(expression, declaratedVariable, nameTable);
            expressionParser.parseExpression();

            CodeGenerator.addInstruction("jmp cont");
        }

        CodeGenerator.addInstruction("cont:");

        return true;

    }


    private boolean isExpression(List<Keyword> codeLine) throws LexicalAnalyzeException {
        return nameTable.getIdentifier(codeLine.get(0).word).getCategory().equals(tCat.VAR);
    }

    private boolean isElse() {
        return conditionArea.stream().anyMatch(list -> list.get(0).word.equals(Command.ELSE.getName()));
    }

    private void parseIfThen(List<Keyword> ifThenCodeLine) throws ConditionAnalyzeException, LexicalAnalyzeException {

        if (!nameTable.getIdentifier(ifThenCodeLine.get(1).word).getCategory().equals(tCat.VAR)) {
            throw new ConditionAnalyzeException("Ожидается переменная");
        }

        if (!(ifThenCodeLine.get(2).lex.equals(Lexems.EQUAL) || ifThenCodeLine.get(2).lex.equals(Lexems.NOT_EQUAL))) {
            throw new ConditionAnalyzeException("Ожидается операция");
        }

        switch (ifThenCodeLine.get(3).lex) {
            case TRUE:
            case FALSE:
                break;
            case NAME:
                if (!nameTable.getIdentifier(ifThenCodeLine.get(3).word).getCategory().equals(tCat.VAR)) {
                    throw new ConditionAnalyzeException("Ожидается переменная");
                }
                break;
            default:
                throw new ConditionAnalyzeException("Ожидается переменная или значение");
        }

        if (!ifThenCodeLine.get(4).word.equals(Command.THEN.getName())) {
            throw new ConditionAnalyzeException("Ожидается команда THEN");
        }

        CodeGenerator.addInstruction("mov ax," + ifThenCodeLine.get(1).word);
        StringBuilder value = new StringBuilder();
        value.append(
                ifThenCodeLine.get(3).lex.equals(Lexems.NAME) ?
                        ifThenCodeLine.get(3).word : ifThenCodeLine.get(3).word + "b"
        );
        CodeGenerator.addInstruction("mov bx," + value);
        value = new StringBuilder();
        CodeGenerator.addInstruction("cmp ax,bx");
        value.append(ifThenCodeLine.get(2).lex.equals(Lexems.EQUAL) ? "je" : "jne");
        CodeGenerator.addInstruction(value + " if");

        if (isElse()) {
            if (value.toString().equals("je")) {
                CodeGenerator.addInstruction("jne else");
            } else {
                CodeGenerator.addInstruction("je else");
            }
        } else {
            CodeGenerator.addInstruction("jmp cont");
        }

    }
}
