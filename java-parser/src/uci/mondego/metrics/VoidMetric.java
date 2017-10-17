package uci.mondego.metrics;

/* For demonstration purposes
 * Shows sample code to visit an AST and extract some information
 *
 * Prints all method declarations
 */

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class VoidMetric {
    public static void run(CompilationUnit cUnit) {
        try {
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    super.visit(n, arg);
                    System.out.println("-> " + n.getDeclarationAsString());
                }
            }.visit(cUnit, null);
            System.out.println(); // empty line
        } catch (Exception e) {
            new RuntimeException(e);
        }
    }
}
