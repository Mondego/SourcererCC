package uci.mondego;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<MetricCollector>  {
    
    @Override
    public void visit(IfStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.numIf++;
        arg.NOS++;
    }
    
    @Override
    public void visit(ForStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.LOOP++;
        arg.NOS++;
    }
    
    @Override
    public void visit(ForeachStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.LOOP++;
        arg.NOS++;
    }
    
    
}
