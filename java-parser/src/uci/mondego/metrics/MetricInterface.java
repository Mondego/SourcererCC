package uci.mondego.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Created by Farima on 10/21/2017.
 */
public interface MetricInterface {

       int calculate(Node node);

}
