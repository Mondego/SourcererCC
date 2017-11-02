package uci.mondego;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsStmt;
import com.github.javaparser.ast.modules.ModuleOpensStmt;
import com.github.javaparser.ast.modules.ModuleProvidesStmt;
import com.github.javaparser.ast.modules.ModuleRequiresStmt;
import com.github.javaparser.ast.modules.ModuleUsesStmt;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<MetricCollector>  {
    
    @Override
    public void visit(IfStmt n, MetricCollector arg) {
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
    
    @Override
    public void visit(AssertStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }
    
    @Override
    public void visit(AssignExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    @Override
    public void visit(ArrayAccessExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    
    @Override
    public void visit(ArrayCreationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    
    @Override
    public void visit(BinaryExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    
    @Override
    public void visit(BlockStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }
    
    @Override
    public void visit(BooleanLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    @Override
    public void visit(BreakStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }
    
    @Override
    public void visit(CastExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
        arg.CAST++;
    }
    
    @Override
    public void visit(CharLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    @Override
    public void visit(ClassExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }
    @Override
    public void visit(ConditionalExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
        System.out.println(":: "+n);
    }
    @Override
    public void visit(ContinueStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }
    @Override
    public void visit(DoStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
        arg.LOOP++;
    }
    
    @Override
    public void visit(DoubleLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(AnnotationDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationLevel n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayInitializerExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(ArrayType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(BlockComment n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(EmptyStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(EnclosedExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(EnumConstantDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ExpressionStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        //System.out.println(":: "+ n);
        arg.NOS++;
    }

    @Override
    public void visit(FieldAccessExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(FieldDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(InstanceOfExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(IntegerLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(IntersectionType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(LabeledStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(LambdaExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(LineComment n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(LongLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(MarkerAnnotationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(MemberValuePair n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(MethodReferenceExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(ModuleDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleExportsStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ModuleOpensStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ModuleProvidesStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ModuleRequiresStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ModuleUsesStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(Name n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(NodeList n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(NormalAnnotationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(NullLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(ObjectCreationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(PackageDeclaration n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(Parameter n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(PrimitiveType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(SimpleName n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(StringLiteralExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(SuperExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(SwitchEntryStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(SwitchStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(SynchronizedStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(ThisExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(ThrowStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(TryStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(TypeExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(TypeParameter n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(UnaryExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(UnionType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(UnknownType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(UnparsableStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
    }

    @Override
    public void visit(VariableDeclarationExpr n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NEXP++;
    }

    @Override
    public void visit(VariableDeclarator n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(VoidType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
        arg.NOS++;
        arg.LOOP++;
    }

    @Override
    public void visit(WildcardType n, MetricCollector arg) {
        // TODO Auto-generated method stub
        super.visit(n, arg);
    }
    
}
