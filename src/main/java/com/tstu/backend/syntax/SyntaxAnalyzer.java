package com.tstu.backend.syntax;

import com.tstu.backend.IConditionParser;
import com.tstu.backend.INameTable;
import com.tstu.backend.ISyntaxAnalyzer;
import com.tstu.backend.exceptions.*;
import com.tstu.backend.generator.CodeGenerator;
import com.tstu.backend.model.Identifier;
import com.tstu.backend.model.Keyword;
import com.tstu.backend.model.enums.Command;
import com.tstu.backend.model.enums.Lexems;
import com.tstu.backend.model.enums.tCat;
import com.tstu.backend.structures.ConditionParser;
import com.tstu.backend.structures.ExpressionParser;
import com.tstu.util.CustomLogger;
import com.tstu.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyntaxAnalyzer implements ISyntaxAnalyzer {

    private Logger logger = new CustomLogger(SyntaxAnalyzer.class.getName());

    private List<Keyword> lexems;
    private INameTable nameTable;

    private List<List<Keyword>> codeLines;
    private List<Identifier> declaratedVariable;

    public SyntaxAnalyzer(List<Keyword> lexems, INameTable nameTable) {
        this.lexems = lexems;
        this.nameTable = nameTable;
        codeLines = new ArrayList<>();
        declaratedVariable = new ArrayList<>();
    }

    private void splitIntoCodeLines() {
        List<Keyword> codeLine = new ArrayList<>();
        for (int i = 0; i < lexems.size(); i++) {
            if (lexems.get(i).lex != Lexems.SPLITTER) {
                codeLine.add(lexems.get(i));
            } else {
                codeLine.add(lexems.get(i));
                codeLines.add(codeLine);
                codeLine = new ArrayList<>();
            }
        }
    }

    private void parseVariableDeclaration() throws SyntaxAnalyzeException, LexicalAnalyzeException {
        List<Keyword> varDeclareCodeLine = codeLines.get(0);

        int colonIndex = 0;
        for (Keyword keyword : varDeclareCodeLine) {
            if (keyword.lex != Lexems.COLON) {
                colonIndex++;
            } else break;
        }

        if (colonIndex == varDeclareCodeLine.size()) {
            throw new SyntaxAnalyzeException("Пропущен символ \":\" ");
        }

        List<Keyword> typeDeclaration = varDeclareCodeLine.stream().limit(colonIndex).collect(Collectors.toList());
        List<Keyword> varEnumeration = varDeclareCodeLine.stream().skip(colonIndex).collect(Collectors.toList());

        parseTypeDeclaration(typeDeclaration);
        parseVarEnumeration(varEnumeration);

    }

    private List<List<Keyword>> singleOutPartOfCode(Command start, Command end) throws SyntaxAnalyzeException {
        int startIndex = 0;

        if (codeLines.stream().filter(e -> e.get(0).word.equals(start.getName())).count() !=
                codeLines.stream().filter(e -> e.get(0).word.equals(end.getName())).count()) {
            throw new SyntaxAnalyzeException(" Не найдена открывающая или закрывающая команда");
        }

        for (List<Keyword> codeline : codeLines) {
            if (!codeline.get(0).word.equals(start.getName())) {
                startIndex++;
            } else break;
        }

        if (startIndex == codeLines.size()) {
            throw new SyntaxAnalyzeException("Пропущена команда " + start.getName());
        }

        int endIndex = 0;
        for (List<Keyword> codeline : codeLines) {
            if (!codeline.get(0).word.equals(end.getName())) {
                endIndex++;
            } else break;
        }


        if (endIndex == codeLines.size()) {
            throw new SyntaxAnalyzeException("Пропущена команда " + end.getName());
        }

        if (startIndex > endIndex) {
            throw new SyntaxAnalyzeException(end.getName() + " встречается раньше " + start.getName());
        }

        return codeLines.subList(startIndex, endIndex + 1);
    }

    private void parseProcedureCode() throws SyntaxAnalyzeException, ExpressionAnalyzeException, ConditionAnalyzeException, LexicalAnalyzeException, WhileAnalyzeException {

        List<List<Keyword>> mainArea = singleOutPartOfCode(Command.BEGIN, Command.END);

        for (int i = 0; i < mainArea.size(); i++) {
            List<Keyword> codeline = mainArea.get(i);
            Identifier currentIdentifier = nameTable.getIdentifier(codeline.get(0).word);
            switch (currentIdentifier.getCategory()) {
                case COMMAND:
                    if (currentIdentifier.getName().equals(Command.PRINT.getName())) {
                        parsePrintCommand(codeline);
                    }
                    if (currentIdentifier.getName().equals(Command.IF.getName())) {
                        List<List<Keyword>> conditionArea = singleOutPartOfCode(Command.IF, Command.ENDIF);
                        IConditionParser conditionParser = new ConditionParser(conditionArea, declaratedVariable,  nameTable);
                        conditionParser.parseCondition();
                        i += conditionArea.size() - 1;
                    }
                    break;
                case VAR:
                    List<Keyword> expression = new ArrayList<>(codeline);
                    parseExpression(expression);
                    break;
            }
        }
    }

    private void parsePrintCommand(List<Keyword> codeline) {
        CodeGenerator.addInstruction("push ax");
        CodeGenerator.addInstruction("mov ax, " + codeline.get(1).word);
        CodeGenerator.addInstruction("CALL PRINT");
        CodeGenerator.addInstruction("pop ax");
    }

    private void parseExpression(List<Keyword> expression) throws ExpressionAnalyzeException, LexicalAnalyzeException {
        ExpressionParser expressionParser = new ExpressionParser(expression, declaratedVariable, nameTable);
        expressionParser.parseExpression();
    }

    private void parseVarEnumeration(List<Keyword> varEnumeration) throws SyntaxAnalyzeException, LexicalAnalyzeException {
        Lexems expected = Lexems.NAME;
        for (int i = 1; i < varEnumeration.size() - 1; i++) { // -1 тк последний символ \n
            if (varEnumeration.get(i).lex != expected) {
                throw new SyntaxAnalyzeException("Лексема не соответствует ожидаемой");
            }
            switch (varEnumeration.get(i).lex) {
                case NAME:
                    if (nameTable.getIdentifier(varEnumeration.get(i).word).getCategory() != tCat.VAR) {
                        throw new SyntaxAnalyzeException("Ожидается переменная");
                    }
                    declaratedVariable.add(nameTable.getIdentifier(varEnumeration.get(i).word));
                    CodeGenerator.addInstruction(varEnumeration.get(i).word + " dw 0b");
                    expected = Lexems.SEMI;
                    break;
                case SEMI:
                    expected = Lexems.NAME;
                    break;
            }
        }
        CodeGenerator.declareStackAndCodeSegments();
    }

    private void parseTypeDeclaration(List<Keyword> typeDeclaration) throws SyntaxAnalyzeException, LexicalAnalyzeException {
        Identifier dataTypeIdentifier = nameTable.getIdentifier(typeDeclaration.get(0).word);
        if (!dataTypeIdentifier.getCategory().equals(tCat.TYPE)) {
            throw new SyntaxAnalyzeException("Тип данных не найден");
        }
        for (Identifier varIdentifier : nameTable.getIdentifiers()) {
            if (varIdentifier.getCategory() == tCat.VAR) {
                varIdentifier.setType(dataTypeIdentifier.getType());
            }
        }

    }

    public boolean checkSyntax() throws SyntaxAnalyzeException, ExpressionAnalyzeException, ConditionAnalyzeException, LexicalAnalyzeException, WhileAnalyzeException {
        logger.info("\n---Синтаксический анализ---\n");
        splitIntoCodeLines();
        parseVariableDeclaration();
        parseProcedureCode();
        logger.info("\nOK!");

        return true;
    }

}
