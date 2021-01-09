package ru.tver.tstu;

import ru.tver.tstu.backend.INameTable;
import ru.tver.tstu.backend.ISyntaxAnalyzer;
import ru.tver.tstu.backend.exceptions.LexicalAnalyzeException;
import ru.tver.tstu.backend.lexems.IdentifierTable;
import ru.tver.tstu.backend.lexems.LexicalAnalyzer;
import ru.tver.tstu.backend.model.Keyword;
import ru.tver.tstu.backend.syntax.SyntaxParserWithPl0StackCodeGen;
import ru.tver.tstu.util.FileReader;

import java.util.List;

public class SyntaxAndPl0CheckApp {

    public static void main(String[] args) throws LexicalAnalyzeException {
        String data = FileReader.parseFromFile("src/main/resources/text3.txt");
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();

        INameTable nameTable = new IdentifierTable();
        List<Keyword> lexems = lexicalAnalyzer.recognizeAllLexem(data);
        nameTable.recognizeAllIdentifiers(lexems);

        ISyntaxAnalyzer syntaxAnalyzer = new SyntaxParserWithPl0StackCodeGen(lexems,nameTable);
        syntaxAnalyzer.checkSyntax();

    }
}
