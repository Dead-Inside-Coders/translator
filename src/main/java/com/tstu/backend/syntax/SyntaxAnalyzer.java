package com.tstu.backend.syntax;

import com.tstu.backend.ILexicalAnalyzer;
import com.tstu.backend.INameTable;
import com.tstu.backend.ISyntaxAnalyzer;
import com.tstu.backend.exceptions.ExpressionAnalyzeException;
import com.tstu.backend.exceptions.LexicalAnalyzeException;
import com.tstu.backend.exceptions.SyntaxAnalyzeException;
import com.tstu.backend.expressions.ExpressionParser;
import com.tstu.backend.lexems.IdentifierTable;
import com.tstu.backend.lexems.LexicalAnalyzer;
import com.tstu.backend.model.Identifier;
import com.tstu.backend.model.Keyword;
import com.tstu.backend.model.enums.Command;
import com.tstu.backend.model.enums.Lexems;
import com.tstu.backend.model.enums.tCat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyntaxAnalyzer implements ISyntaxAnalyzer {

    private String data;
    private ILexicalAnalyzer lexicalAnalyzer;
    private INameTable nameTable;

    private List<List<Keyword>> codeLines;

    public SyntaxAnalyzer(String data) {
        this.data = data;
        lexicalAnalyzer = new LexicalAnalyzer();
        nameTable = new IdentifierTable();
        codeLines = new ArrayList<>();
    }

    private void splitIntoCodeLines() throws LexicalAnalyzeException {
        List<Keyword> lexemsList = lexicalAnalyzer.recognizeAllLexem(data);
        nameTable.recognizeAllIdentifiers(lexemsList);

        List<Keyword> codeLine = new ArrayList<>();
        for (int i = 0; i < lexemsList.size(); i++) {

            if (lexemsList.get(i).lex != Lexems.SPLITTER) {
                codeLine.add(lexemsList.get(i));
            } else {
                codeLine.add(lexemsList.get(i));
                codeLines.add(codeLine);
                codeLine = new ArrayList<>();
            }
        }

    }

    private void parseVariableDeclaration() throws SyntaxAnalyzeException {
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

        List<Keyword> varEnumeration = varDeclareCodeLine.stream().limit(colonIndex).collect(Collectors.toList());
        List<Keyword> typeDeclaration = varDeclareCodeLine.stream().skip(colonIndex).collect(Collectors.toList());

        parseVarEnumeration(varEnumeration);
        parseTypeDeclaration(typeDeclaration);
    }

    private void parseVariableAssign() throws SyntaxAnalyzeException, ExpressionAnalyzeException {
        int beginIndex = 1;

        int endIndex = 0;
        for (List<Keyword> codeline : codeLines) {
            if (!codeline.get(0).word.equals(Command.END.getName())) {
                endIndex++;
            } else break;
        }

        if (endIndex == codeLines.size()) {
            throw new SyntaxAnalyzeException("Пропущена команда \"END\" ");
        }

        List<List<Keyword>> mainArea = codeLines.subList(beginIndex + 1, endIndex);

        for (List<Keyword> codeline : mainArea) {
            List<Keyword> expression = new ArrayList<>(codeline);
            parseExpression(expression);
        }

    }

    private void parseExpression(List<Keyword> expression) throws ExpressionAnalyzeException {
        ExpressionParser expressionParser = new ExpressionParser(expression, nameTable);
        expressionParser.parseExpression();
    }

    private void parseVarEnumeration(List<Keyword> varEnumeration) throws SyntaxAnalyzeException {
        if (!nameTable.getIdentifier(varEnumeration.get(0).word).getName().equals(Command.VAR.getName())) {
            throw new SyntaxAnalyzeException("Ключевое слово Var не найдено");
        }

        Lexems expected = Lexems.NAME;
        for (int i = 1; i < varEnumeration.size(); i++) {
            if (varEnumeration.get(i).lex != expected) {
                throw new SyntaxAnalyzeException("Лексема не соответствует ожидаемой");
            }
            switch (varEnumeration.get(i).lex) {
                case NAME:
                    if (nameTable.getIdentifier(varEnumeration.get(i).word).getCategory() != tCat.VAR) {
                        throw new SyntaxAnalyzeException("Ожидается переменная");
                    }
                    expected = Lexems.SEMI;
                    break;
                case SEMI:
                    expected = Lexems.NAME;
                    break;
            }
        }
    }

    private void parseTypeDeclaration(List<Keyword> typeDeclaration) throws SyntaxAnalyzeException {
        Identifier dataTypeIdentifier = nameTable.getIdentifier(typeDeclaration.get(1).word);
        if (!dataTypeIdentifier.getCategory().equals(tCat.TYPE)) {
            throw new SyntaxAnalyzeException("Тип данных не найден");
        }
        for (Identifier varIdentifier : nameTable.getIdentifiers()) {
            if (varIdentifier.getCategory() == tCat.VAR) {
                varIdentifier.setType(dataTypeIdentifier.getType());
            }
        }

    }

    @Override
    public void checkSyntax() throws SyntaxAnalyzeException, LexicalAnalyzeException, ExpressionAnalyzeException {
        splitIntoCodeLines();
        parseVariableDeclaration();
        parseVariableAssign();
    }

    public static void main(String[] args) throws LexicalAnalyzeException, SyntaxAnalyzeException, ExpressionAnalyzeException {
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(
                "Var a,b,c :Logical\n" +
                        "Begin\n" +
                        "a:=0\n" +
                        "b:=1\n" +
                        "c:= a | b & a | b\n" + // 0 | 1 & 0 | 1
                        "End\n"
        );

        syntaxAnalyzer.checkSyntax();

        System.out.println("OK");
    }
}