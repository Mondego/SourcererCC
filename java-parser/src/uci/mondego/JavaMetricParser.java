package uci.mondego;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        JavaMetricParser mParser = new JavaMetricParser();
        Vector<File> files = new DirExplorer().finder("java_samples");

        for (File f : files) {
            mParser.metricalize(f);
        }
    }

    public void metricalize(File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());

        CompilationUnit cUnit = new JavaParser().parse(file);

        //for (MethodDeclaration method : cUnit.getMethods()) {
        // Make the visitor go through everything inside the method.
        // method.accept(new MethodCallVisitor(), null);
        //	System.out.println("Inside");
        //}


        System.out.println("   with the imports: " + cUnit.getImports());
    }

}