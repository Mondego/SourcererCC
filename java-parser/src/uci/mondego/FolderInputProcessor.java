package uci.mondego;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class FolderInputProcessor implements IInputProcessor{
    
    @Override
    public void processInput(String filename) throws FileNotFoundException {
        System.out.println("processing directory: " + filename);
        List<File> files = DirExplorer.finder(filename);
        for (File f : files) {
            try {
                this.metricalize(f);
            } catch (FileNotFoundException e) {
                System.out.println("WARN: File not found, skipping file: " + f.getAbsolutePath());
                try {
                    FileWriters.errorPw.write(f.getAbsolutePath() + System.lineSeparator());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseProblemException e) {
                System.out.println("WARN: parse problem exception, skippig file: " + f.getAbsolutePath());
                try {
                    FileWriters.errorPw.write(f.getAbsolutePath() + System.lineSeparator());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("WARN: unknown error, skippig file: " + f.getAbsolutePath());
                try {
                    FileWriters.errorPw.write(f.getAbsolutePath() + System.lineSeparator());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
    private void metricalize(final File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        JavaMetricParser.FILE_COUNTER++;
        CompilationUnit cu = JavaParser.parse(file);
        TreeVisitor visitor = new CustomVisitor(file);
        visitor.visitPreOrder(cu);
    }

}
