package uci.mondego;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import uci.mondego.metrics.VoidMetric;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        Vector<File> files = DirExplorer.finder("java_samples");

        for (File f : files) {
            metricalize(f);
        }
    }

    public static void metricalize(File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());

        CompilationUnit cUnit = JavaParser.parse(file);

        // Run one metric
        VoidMetric.run(cUnit);
    }

}