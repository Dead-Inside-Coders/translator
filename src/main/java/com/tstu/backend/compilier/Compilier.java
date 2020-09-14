package com.tstu.backend.compilier;

import com.tstu.backend.ILexicalAnalyzer;
import com.tstu.backend.INameTable;
import com.tstu.backend.ISyntaxAnalyzer;
import com.tstu.backend.exceptions.ExpressionAnalyzeException;
import com.tstu.backend.exceptions.LexicalAnalyzeException;
import com.tstu.backend.exceptions.SyntaxAnalyzeException;
import com.tstu.backend.generator.CodeGenerator;
import com.tstu.backend.lexems.IdentifierTable;
import com.tstu.backend.lexems.LexicalAnalyzer;
import com.tstu.backend.model.Keyword;
import com.tstu.backend.syntax.SyntaxAnalyzer;
import com.tstu.util.CustomLogger;
import com.tstu.util.Logger;

import java.io.IOException;
import java.util.List;

public class Compilier {

    private Logger logger = new CustomLogger("Compilier");

    private ILexicalAnalyzer lexicalAnalyzer;
    private INameTable nameTable;
    private ISyntaxAnalyzer syntaxAnalyzer;

    public Compilier() {
        lexicalAnalyzer = new LexicalAnalyzer();
        nameTable = new IdentifierTable();
    }


    public boolean compile(String data) {
        List<Keyword> lexems;
        try {
            lexems = lexicalAnalyzer.recognizeAllLexem(data);
            nameTable.recognizeAllIdentifiers(lexems);
        } catch (LexicalAnalyzeException e) {
            logger.error(e.getMessage());
            return false;
        }

        syntaxAnalyzer = new SyntaxAnalyzer(lexems, nameTable);

        CodeGenerator.declareDataSegment();

        try {
            syntaxAnalyzer.checkSyntax();
        } catch (ExpressionAnalyzeException | SyntaxAnalyzeException e) {
            logger.error(e.getMessage());
            return false;
        }

        CodeGenerator.declareEndMainProcedure();
        CodeGenerator.declarePrintProcedure();
        CodeGenerator.declareEndProgram();

        try {
            CodeGenerator.generateFile();
          //  Executor.execute();
        } catch (IOException  ex) {
            logger.error("Ошибка записи файла " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }

        return true;
    }

}
