package uci.mondego;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import static com.github.javaparser.JavaParser.parse;
import static uci.mondego.DirExplorer.finder;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        Vector<File> files = finder("java_samples");

        for (File f : files) {
            metricalize(f);
        }
    }

    public static void metricalize(File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());

        CompilationUnit cUnit = parse(file);

        //for (MethodDeclaration method : cUnit.getMethods()) {
        // Make the visitor go through everything inside the method.
        // method.accept(new MethodCallVisitor(), null);
        //	System.out.println("Inside");
        //}


        System.out.println("   with the imports: " + cUnit.getImports());
    }

}