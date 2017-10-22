package uci.mondego.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.symbolsolver.resolution.typeinference.MethodType;

import java.util.List;

/**
 * Created by Farima on 10/21/2017.
 */
public class LOOP implements MetricInterface {
    int numLoop=0;
    public int calculate(Node node){
        try {
            TreeVisitor test= new TreeVisitor(){
                @Override
                public void process(Node node) {
                    //visitPreOrder(node);
                    if (node instanceof ForStmt || node instanceof ForeachStmt || node instanceof WhileStmt)
                        numLoop++;
                }
            };

            test.visitPreOrder(node);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return numLoop;
    }
}
