package ru.tver.tstu;

import ru.tver.tstu.backend.lexems.IdentifierTable;
import ru.tver.tstu.backend.lexems.LexicalAnalyzer;
import ru.tver.tstu.util.FileReader;

public class IdentifierCheckApp {

    public static void main(String[] args) {
       String data = FileReader.parseFromSourceCodeFile("src/main/resources/text3.txt");

        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();

        IdentifierTable nameTable = new IdentifierTable();

        nameTable.recognizeAllIdentifiers(lexicalAnalyzer.recognizeAllLexem(data));

    }

}