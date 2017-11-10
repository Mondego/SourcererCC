package uci.mondego;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class JavaMetricParser {
    public PrintWriter pw=null;
    public static void main(String[] args) throws FileNotFoundException {
        JavaMetricParser javaMetricParser=new JavaMetricParser();
        List<File> files = DirExplorer.finder("java_samples");

        for (File f : files) {
            if (f.getName().equals("JhawkTest.java")) {
                javaMetricParser.metricalize(f);
            }
        }
    }

    public  void metricalize(final File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        CompilationUnit cu = JavaParser.parse(file);
        TreeVisitor astVisitor = new TreeVisitor() {


            @Override
            public void process(Node node) {
                if (node instanceof MethodDeclaration || node instanceof ConstructorDeclaration) {
                   // JavaParser.parse(((MethodDeclaration) node).getBody().get().toString()).get;
                    //System.out.println(((MethodDeclaration) node).getBody().get().getTokenRange().get().getBegin());
                    MetricCollector collector = new MetricCollector();
                    collector._file = file;
                    //collector.NTOKENS = ((MethodDeclaration) node).getModifiers().size();
                    for (Modifier c : ((MethodDeclaration) node).getModifiers()){
                        collector.addToken(c.toString());
                        collector.removeFromOperands.add(c.toString());
                        collector.MOD++;
                    }
                    
                    NodeList<ReferenceType> exceptionsThrown = ((MethodDeclaration) node).getThrownExceptions();
                    if (exceptionsThrown.size()>0){
                        collector.addToken("throws");
                    }
                    //collector.EXCT = exceptionsThrown.size();
                    NodeList<Parameter> nl = ((MethodDeclaration) node).getParameters();
                    for(Parameter p : nl){
                        
                        for(Node c: p.getChildNodes()){
                            if(c instanceof SimpleName)
                                collector.parameterList.add(c.toString());
                        }
                    }
                    collector.NOA = nl.size();
                    collector.START_LINE = node.getBegin().get().line;
                    collector.END_LINE = node.getEnd().get().line;
                    collector._methodName = ((MethodDeclaration) node).getName().asString();
                    collector.removeFromOperands.add(collector._methodName);
                    node.accept(new MethodVisitor(), collector);
                    collector.computeHalsteadMetrics();
                    collector.COMP++; // add 1 for the default path.
                    collector.NOS++; // accounting for method declaration
                    collector.innerMethods.remove(collector._methodName);
                    collector.innerMethodParameters = (List<String>) CollectionUtils.subtract(collector.innerMethodParameters , collector.parameterList);
                    collector.populateVariableRefList();
                    Collections.sort(collector.variablesRefList);
                    Collections.sort(collector.variableDeclaredList);
                    Collections.sort(collector.operands);
                    Collections.sort(collector.typeList);
                    Collections.sort(collector.operators);
                    Collections.sort(collector.tokens);
                    this.writeToCsv(collector);
                    System.out.println(collector._methodName+", MDN: "+collector.MDN +", TDN: "+ collector.TDN);
                    System.out.println(collector);
                }
            }
            public void writeToCsv(MetricCollector collector){
                try {
                    if (pw==null) pw = new PrintWriter("output/metrics.csv");
                    StringBuilder line=new StringBuilder("");
                    line.append(collector.START_LINE).append("~~").append(collector.END_LINE).append("~~").append(collector._file).append("~~").
                            append(collector._methodName).append("~~").append(collector.COMP).append("~~").append(collector.NOCL).append("~~").
                            append(collector.NOS).append("~~").append(collector.HLTH).append("~~").append(collector.HVOC).append("~~").
                            append(collector.HEFF).append("~~").append(collector.HBUG).append("~~").append(collector.CREF).append("~~").
                            append(collector.XMET).append("~~").append(collector.LMET).append("~~").append(collector.NLOC).append("~~").
                            append(collector.NOC).append("~~").append(collector.NOA).append("~~").append(collector.MOD).append("~~").
                            append(collector.uniqueName).append("~~").append(collector.HDIF).append("~~").append(collector.VDEC).append("~~").
                            append(collector.EXCT).append("~~").append(collector.EXCR).append("~~").append(collector.CAST).append("~~").
                            append(collector.TDN).append("~~").append(collector.HVOL).append("~~").append(collector.NAND).append("~~").
                            append(collector.simpleUniqueName).append("~~").append(collector.VREF).append("~~").append(collector.NOPR).append("~~").
                            append(collector.MDN).append("~~").append(collector.NEXP).append("~~").append(collector.LOOP).append(System.lineSeparator());
                    pw.append(line);
                    pw.append(System.lineSeparator());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            
        };
        astVisitor.visitPreOrder(cu);
        pw.close();

    }
    
}