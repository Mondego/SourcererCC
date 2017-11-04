package uci.mondego;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        List<File> files = DirExplorer.finder("java_samples");

        for (File f : files) {
            if (f.getName().equals("JhawkTest.java")) {
                metricalize(f);
            }
        }
    }

    public static void metricalize(final File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        CompilationUnit cu = JavaParser.parse(file);
        TreeVisitor astVisitor = new TreeVisitor() {
            @Override
            public void process(Node node) {
                if (node instanceof MethodDeclaration || node instanceof ConstructorDeclaration) {
                   // JavaParser.parse(((MethodDeclaration) node).getBody().get().toString()).get;
                    //System.out.println(((MethodDeclaration) node).getBody().get().getTokenRange().get().getBegin());
                    
                    MetricCollector collector = new MetricCollector();
                    collector.file = file;
                    collector.NOA = ((MethodDeclaration) node).getParameters().size();
                    collector.START_LINE = node.getBegin().get().line;
                    collector.END_LINE = node.getEnd().get().line;
                    collector.methodName = ((MethodDeclaration) node).getName().asString();
                    node.accept(new MethodVisitor(), collector);
                    System.out.println(collector);
                }
            }
        };
        astVisitor.visitPreOrder(cu);

    }
}