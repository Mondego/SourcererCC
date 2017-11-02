package uci.mondego;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        List<File> files = DirExplorer.finder("java_samples");

        for (File f : files) {

            metricalize(f);
        }
    }

    public static void metricalize(File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        CompilationUnit cu = JavaParser.parse(file);
        TreeVisitor astVisitor = new TreeVisitor() {
            @Override
            public void process(Node node) {
                if (node instanceof MethodDeclaration) {
                    MetricCollector collector = new MetricCollector();
                    collector.startLine = node.getBegin().get().line;
                    collector.endLine = node.getEnd().get().line;
                    collector.methodName = ((MethodDeclaration) node).getName()
                            .asString();
                    node.accept(new MethodVisitor(), collector);
                    System.out.println(collector);
                }
            }
        };
        astVisitor.visitPreOrder(cu);

    }
}