package uci.mondego;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class JavaMetricParser {

    public static void main(String[] args) throws FileNotFoundException {
        Vector<File> files = DirExplorer.finder("java_samples");

        for (File f : files) {

            metricalize(f);
        }
    }

    public static void metricalize(File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        MethodVisitor visitor = new MethodVisitor();
        CompilationUnit cu = JavaParser.parse(file);
        // cu.accept(visitor, null);
        // visitor.visit(cu, null);
        // cu.accept(visitor, null);
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
                    // System.out.println(((MethodDeclaration)node).getDeclarationAsString());
                    // System.out.println("num of loops "+new
                    // LOOP().calculate(node));
                }
            }
        };
        astVisitor.visitPreOrder(cu);

        // Run one metric
        // new VoidMetric().run(fileNode);

    }
}