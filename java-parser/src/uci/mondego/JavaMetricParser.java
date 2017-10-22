package uci.mondego;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.TreeVisitor;
import uci.mondego.metrics.LOOP;
import uci.mondego.metrics.VoidMetric;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
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

        CompilationUnit fileNode = JavaParser.parse(file);
        //System.out.println(fileNode.toString());
        TreeVisitor methodVisitor=new TreeVisitor(){
            @Override
            public void process(Node node) {
                if (node instanceof MethodDeclaration) {
                    System.out.println(((MethodDeclaration)node).getDeclarationAsString());
                    System.out.println("num of loops "+new LOOP().calculate(node));
                }
            }
        };
        methodVisitor.visitPreOrder(fileNode);

        // Run one metric
        //new VoidMetric().run(fileNode);


    }

}