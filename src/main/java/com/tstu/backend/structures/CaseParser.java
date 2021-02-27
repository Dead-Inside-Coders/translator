/**
 * Copyright 2020 EPAM Systems
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tstu.backend.structures;

import com.tstu.backend.*;
import com.tstu.backend.exceptions.*;
import com.tstu.backend.generator.*;
import com.tstu.backend.lexems.*;
import com.tstu.backend.model.*;
import com.tstu.backend.model.enums.*;

import java.util.*;
import java.util.stream.*;

public class CaseParser
{
    private List<List<Keyword>> caseArea;
    private INameTable nameTable;
    private List<Identifier> declaratedVariable;
    Identifier caseVariable;
    private List<List<Keyword>> caseConditionsArea;

    public CaseParser(List<List<Keyword>> caseArea, List<Identifier> declaratedVariable, INameTable nameTable)
    {
        this.caseArea = caseArea;
        this.nameTable = nameTable;
        this.declaratedVariable = declaratedVariable;
    }

    public boolean parseCaseArea() throws LexicalAnalyzeException, CaseAnalyzeException, ExpressionAnalyzeException {
        parseCaseOf(caseArea.get(0));

        CodeGenerator.addInstruction("mov ax," + caseVariable.getName());

        caseConditionsArea = caseArea.subList(1, caseArea.size() - 1);
        parseCaseConditions();

        CodeGenerator.addInstruction("cont: ");
        return true;
    }

    private void parseCaseOf(List<Keyword> caseOfCodeLine) throws LexicalAnalyzeException, CaseAnalyzeException {
        caseVariable = nameTable.getIdentifier(caseOfCodeLine.get(1).word);

        if (!caseVariable.getCategory().equals(tCat.VAR))
        {
            throw new CaseAnalyzeException("Ожидается переменная");
        }

        if (!caseOfCodeLine.get(2).word.equals(Command.OF.getName()))
        {
            throw new CaseAnalyzeException("Ожидается команда " + Command.OF.getName());
        }

    }

    private void parseCaseConditions() throws CaseAnalyzeException, ExpressionAnalyzeException, LexicalAnalyzeException {
        generateLabels(generateCmpJe());
    }

    private List<String> generateCmpJe() throws CaseAnalyzeException {
        int maxLabelNumber = 0;
        List<String> labelsList = new ArrayList<>();

        for (List<Keyword> condition : caseConditionsArea)
        {
            String labelName = "label" + maxLabelNumber++;

            if (!(condition.get(0).lex.equals(Lexems.TRUE) || condition.get(0).lex.equals(Lexems.FALSE)))
            {
                throw new CaseAnalyzeException("Ожидается константа(булеан)");
            }

            if (!condition.get(1).lex.equals(Lexems.COLON))
            {
                throw new CaseAnalyzeException("Ожидается двоеточие");
            }

            CodeGenerator.addInstruction("mov bx," + condition.get(0).word + "b");
            CodeGenerator.addInstruction("cmp ax,bx");
            CodeGenerator.addInstruction("je " + labelName);
            labelsList.add(labelName);
        }

        return labelsList;
    }

    private void generateLabels(List<String> labelsList) throws CaseAnalyzeException, LexicalAnalyzeException, ExpressionAnalyzeException {
        for (int i = 0; i < caseConditionsArea.size(); i++)
        {
            List<Keyword> condition = caseConditionsArea.get(i);

            List<Keyword> expression = condition.stream().skip(2).collect(Collectors.toList());

            if (!isExpression(expression))
            {
                throw new CaseAnalyzeException("Ожидается выражение");
            }

            CodeGenerator.addInstruction(labelsList.get(i) + ":");
            ExpressionParser expressionParser = new ExpressionParser(expression, declaratedVariable, nameTable);
            expressionParser.parseExpression();

            CodeGenerator.addInstruction("jmp cont");
        }
    }

    private boolean isExpression(List<Keyword> codeLine) throws LexicalAnalyzeException {
        return nameTable.getIdentifier(codeLine.get(0).word).getCategory().equals(tCat.VAR);
    }



}
