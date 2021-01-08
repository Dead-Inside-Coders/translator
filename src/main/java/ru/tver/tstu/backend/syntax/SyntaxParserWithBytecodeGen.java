package ru.tstu.tver.backend.syntax;

import ru.tstu.tver.backend.generator.bytecode.BCG;
import ru.tstu.tver.backend.INameTable;
import ru.tstu.tver.backend.generator.pl0.PL0CodeGenerator;
import ru.tstu.tver.backend.model.Identifier;
import ru.tstu.tver.backend.model.Keyword;
import ru.tstu.tver.backend.model.enums.Command;
import ru.tstu.tver.backend.model.enums.IdentifierCategory;
import ru.tstu.tver.backend.model.enums.Lexem;
import ru.tstu.tver.backend.model.enums.OpCode;
import ru.tstu.tver.backend.structures.ExpressionParser;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SyntaxParserWithBytecodeGen extends RecursiveDescentParser {

    private List<Keyword> currentExpression;

    private int currentDataAddress = 3; // because exists system vars RA,DL,SL
    private int currentLevel = 0;

    public SyntaxParserWithBytecodeGen(List<Keyword> lexems, INameTable nameTable) {
        super(lexems, nameTable);
        iterator = lexems.iterator();
        currentExpression = new ArrayList<>();
    }

    @Override
    protected void factor() {
        if (currentKeyword.lex == Lexem.NAME) { // var or const
            currentExpression.add(currentKeyword);
            isAccept(Lexem.NAME);
        } else if (currentKeyword.lex == Lexem.NUMBER) {
            currentExpression.add(currentKeyword);
            isAccept(Lexem.NUMBER);
        } else if (isAccept(Lexem.LEFT_BRACKET)) {
            expression();
            currentExpression.add(currentKeyword);
            isExpect(Lexem.RIGHT_BRACKET, 22);
        } else {
            error(12);
            getNextKeyword();
        }
    }

    @Override
    protected void term() {
        factor();
        while (currentKeyword.lex == Lexem.MULTIPLICATION || currentKeyword.lex == Lexem.DIVISION) {
            currentExpression.add(currentKeyword);
            getNextKeyword();
            factor();
        }
    }

    @Override
    protected void expression() {
        if (currentKeyword.lex == Lexem.ADDITION || currentKeyword.lex == Lexem.SUBTRACTION) {
            currentExpression.add(currentKeyword);
            getNextKeyword();
        }
        term();
        while (currentKeyword.lex == Lexem.ADDITION || currentKeyword.lex == Lexem.SUBTRACTION) {
            currentExpression.add(currentKeyword);
            getNextKeyword();
            term();
        }
    }

    @Override
    protected void condition() {
        if (isAccept(Command.ODD)) {
            evaluateExpression();
            PL0CodeGenerator.addInstruction(OpCode.OPR, 0, "odd");
            PL0CodeGenerator.addInstruction(OpCode.JPC, 0, null);
        } else {
            evaluateExpression();
            Lexem operator = currentKeyword.lex;
            if (operator == Lexem.EQUAL ||
                    operator == Lexem.NOT_EQUAL ||
                    operator == Lexem.LESS_THAN ||
                    operator == Lexem.LESS_OR_EQUAL_THAN ||
                    operator == Lexem.MORE_THAN ||
                    operator == Lexem.MORE_OR_EQUAL_THAN) {
                getNextKeyword();
                evaluateExpression();
                PL0CodeGenerator.addInstruction(OpCode.OPR, 0, operator.getValue());
                PL0CodeGenerator.addInstruction(OpCode.JPC, 0, null);
            } else {
                error(20);
                getNextKeyword();
            }
        }
    }

    private void evaluateExpression() {
        currentExpression.clear();
        expression();
        ExpressionParser expressionParser = new ExpressionParser(currentExpression, identifierTable);
        expressionParser.parseExpression();
    }

    @Override
    protected void statement() {
        Identifier identifier;
        if (currentKeyword.lex == Lexem.NAME) {
            identifier = identifierTable.getIdentifier(currentKeyword.word);
        } else {
            error(11);
            getNextKeyword();
            return;
        }
        if (isAccept(IdentifierCategory.VAR)) {
            isExpect(Lexem.ASSIGN, 19);
            evaluateExpression();
            BCG.addInstr(new VarInsnNode(ISTORE, Integer.parseInt(identifier.getAddress())));
            addPrintByteCodeCommands(identifier.getAddress());
        } else if (isAccept(Command.CALL)) {
            isExpect(Lexem.NAME, 14);
        } else if (isAccept(Command.BEGIN)) {
            do {
                statement();
            } while (isAccept(Lexem.SEMICOLON));
            isExpect(Command.END, 17);
        } else if (isAccept(Command.IF)) {
            condition();
            isExpect(Command.THEN, 16);
            int jumpCodeAddress = PL0CodeGenerator.getLastCodeAddress();
            statement();
            int currentCodeAddress = PL0CodeGenerator.getLastCodeAddress();
            PL0CodeGenerator.changeInstructionAddress(jumpCodeAddress, currentCodeAddress + 1);
        } else if (isAccept(Command.WHILE)) {
            int jmpCodeAddress = PL0CodeGenerator.getLastCodeAddress() + 1;
            condition();
            isExpect(Command.DO, 18);
            int jpcCodeAddress = PL0CodeGenerator.getLastCodeAddress();
            statement();
            int currentCodeAddress = PL0CodeGenerator.getLastCodeAddress();
            PL0CodeGenerator.changeInstructionAddress(jpcCodeAddress, currentCodeAddress + 2);
            PL0CodeGenerator.addInstruction(OpCode.JMP, 0, String.valueOf(jmpCodeAddress));
        } else {
            error(11);
            getNextKeyword();
        }
    }

    private void addPrintByteCodeCommands(String address) {
        BCG.addInstr(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        BCG.addInstr(new VarInsnNode(ILOAD, Integer.parseInt(address)));
        BCG.addInstr(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false));
    }

    @Override
    protected void block() {
        currentLevel++;
        if (isAccept(Command.CONST)) {
            do {
                Identifier identifier = identifierTable.getIdentifier(currentKeyword.word);
                if (currentKeyword.lex == Lexem.NAME) {
                    updateIdentifierInfo(IdentifierCategory.CONST, currentLevel, "0");
                }
                isExpect(Lexem.NAME, 4);
                isExpect(Lexem.EQUAL, 3);
                if (currentKeyword.lex == Lexem.NUMBER) {
                    identifier.setValue(Integer.parseInt(currentKeyword.word));
                }
                isExpect(Lexem.NUMBER, 2);
            } while (isAccept(Lexem.SEMI));
            isExpect(Lexem.SEMICOLON, 5);
        }
        if (isAccept(Command.VAR)) {
            do {
                if (currentKeyword.lex == Lexem.NAME) {
                    updateIdentifierInfo(IdentifierCategory.VAR, currentLevel, String.valueOf(currentDataAddress++));
                }
                isExpect(Lexem.NAME, 4);
            } while (isAccept(Lexem.SEMI));
            isExpect(Lexem.SEMICOLON, 5);
        }
        while (isAccept(Command.PROCEDURE)) {
            if (currentKeyword.lex == Lexem.NAME) {
                updateIdentifierInfo(IdentifierCategory.PROCEDURE_NAME, currentLevel, "0");
            }
            isExpect(Lexem.NAME, 4);
            isExpect(Lexem.SEMICOLON, 5);
            block();
            isExpect(Lexem.SEMICOLON, 5);
        }
        statement();
        currentLevel--;
    }

    private void updateIdentifierInfo(IdentifierCategory category, int level, String address) {
        identifierTable.getIdentifier(currentKeyword.word).setCategory(category);
        identifierTable.getIdentifier(currentKeyword.word).setLevel(level);
        identifierTable.getIdentifier(currentKeyword.word).setAddress(address);
    }

    @Override
    public boolean checkSyntax() {
        program();
        BCG.addInstr(new InsnNode(RETURN));
        return hasErrors;
    }
}