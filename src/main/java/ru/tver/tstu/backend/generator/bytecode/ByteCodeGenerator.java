package ru.tver.tstu.backend.generator.bytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class ByteCodeGenerator {

    private static final String CLASS_FILE_PATH = "file.class"; //root dir

    private static final ClassNode classNode = new ClassNode();


    public static void generateAsFileByPath() {
        generateWrapperClassAndInitMethod();
        generateProgramMethods();
        writeFile();
    }

    public static byte[] generateAsByteArray() {
        generateWrapperClassAndInitMethod();
        generateProgramMethods();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        classNode.accept(cw);

        return cw.toByteArray();
    }

    private static void generateWrapperClassAndInitMethod() {

        classNode.version = V1_8;
        classNode.access = ACC_PUBLIC + ACC_SUPER;
        classNode.name = "ClassTest";
        classNode.superName = "java/lang/Object";
        classNode.interfaces.add("java/lang/Runnable");

        MethodNode initMethodNode = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);

        InsnList initMethodInstructionList = initMethodNode.instructions;

        initMethodInstructionList.add(new VarInsnNode(ALOAD, 0));
        initMethodInstructionList.add(new MethodInsnNode(INVOKESPECIAL, classNode.superName, "<init>", "()V", false));

        initMethodInstructionList.add(new InsnNode(RETURN));

        classNode.methods.add(initMethodNode);

    }

    private static void generateProgramMethods() {

        for (MethodNode node : BCG.getMethods()) {
            classNode.methods.add(node);

        }
        //TODO equals/hashcode
        for (FieldNode node : BCG.getFields()) {
            classNode.fields.add(node);
        }


    }


    private static void writeFile() {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        classNode.accept(cw);

        try (FileOutputStream fos = new FileOutputStream(CLASS_FILE_PATH)) {
            fos.write(cw.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
