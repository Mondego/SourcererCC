package uci.mondego;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class CustomVisitor extends TreeVisitor {
    File file;
    String fileContent;
    Path path;
    SourceCodeLineCounter sourceCodeLineCounter;
    boolean APPLY_SLOC_FILTER = true;
    int MIN_SLOC_ALLOWED = 10;

    public CustomVisitor(File file) {
        super();
        this.file = file;
        this.path = file.toPath();
        sourceCodeLineCounter = new SourceCodeLineCounter();
    }

    public CustomVisitor(String content, Path path) {
        super();
        this.fileContent = content;
        this.path = path;
        sourceCodeLineCounter = new SourceCodeLineCounter();
    }

    @Override
    public void process(Node node) {
        if (node instanceof MethodDeclaration || node instanceof ConstructorDeclaration) {
            JavaMetricParser.METHOD_COUNTER++;
            MetricCollector collector = new MetricCollector();
            collector._file = this.file;
            collector._path = this.path;
            if (node instanceof MethodDeclaration) {
                for (Modifier c : ((MethodDeclaration) node).getModifiers()) {
                    collector.addToken(c.toString());
                    MapUtils.addOrUpdateMap(collector.mapRemoveFromOperands, c.toString());
                    collector.MOD++;
                }
                MethodDeclaration m = (MethodDeclaration) node;
                StringBuilder sb = new StringBuilder();
                try{
                    sb.append(m.getDeclarationAsString()).append(m.getBody().get());
                }catch(Exception e){
                    //ignore this method has no method body
                }
                collector.SLOC = SourceCodeLineCounter.getNumberOfLines(sb.toString().split("[\\r\\n]+"));
            }
            if (node instanceof ConstructorDeclaration) {
                for (Modifier c : ((ConstructorDeclaration) node).getModifiers()) {
                    collector.addToken(c.toString());
                    MapUtils.addOrUpdateMap(collector.mapRemoveFromOperands, c.toString());
                    collector.MOD++;
                }
                ConstructorDeclaration m = (ConstructorDeclaration) node;
                StringBuilder sb = new StringBuilder();
                try{
                    sb.append(m.getDeclarationAsString()).append(m.getBody());
                }catch(Exception e){
                    //ignore this method has no method body
                }
                collector.SLOC = SourceCodeLineCounter.getNumberOfLines(sb.toString().split("[\\r\\n]+"));
            }
            if (this.APPLY_SLOC_FILTER && collector.SLOC < this.MIN_SLOC_ALLOWED) {
                return;
            }
            NodeList<ReferenceType> exceptionsThrown = null;
            if (node instanceof MethodDeclaration) {
                exceptionsThrown = ((MethodDeclaration) node).getThrownExceptions();
            }
            if (node instanceof ConstructorDeclaration) {
                exceptionsThrown = ((ConstructorDeclaration) node).getThrownExceptions();
            }
            if (exceptionsThrown.size() > 0) {
                collector.addToken("throws");
            }
            NodeList<Parameter> nl = null;
            if (node instanceof MethodDeclaration) {
                nl = ((MethodDeclaration) node).getParameters();
            }
            if (node instanceof ConstructorDeclaration) {
                nl = ((ConstructorDeclaration) node).getParameters();
            }
            for (Parameter p : nl) {

                for (Node c : p.getChildNodes()) {
                    if (c instanceof SimpleName)
                        MapUtils.addOrUpdateMap(collector.mapParameter, c.toString());
                }
            }
            collector.NOA = nl.size();
            collector.START_LINE = node.getBegin().get().line;
            collector.END_LINE = node.getEnd().get().line;
            if (node instanceof MethodDeclaration) {
                collector._methodName = ((MethodDeclaration) node).getName().asString();
            }
            if (node instanceof ConstructorDeclaration) {
                collector._methodName = ((ConstructorDeclaration) node).getName().asString();
            }
            MapUtils.addOrUpdateMap(collector.mapRemoveFromOperands, collector._methodName);
            node.accept(new MethodVisitor(), collector);
            collector.computeHalsteadMetrics();
            collector.COMP++; // add 1 for the default path.
            collector.NOS++; // accounting for method declaration
            collector.mapInnerMethods.remove(collector._methodName);
            MapUtils.subtractMaps(collector.mapInnerMethodParameters, collector.mapParameter);
            collector.populateVariableRefList();
            collector.populateMetricHash();
            String fileId = JavaMetricParser.fileIdPrefix + JavaMetricParser.FILE_COUNTER;
            String methodId = fileId + "00" + JavaMetricParser.METHOD_COUNTER;
            // System.out.println("fileId is : " + fileId + ", methodId
            // is: " + methodId);
            collector.fileId = Long.parseLong(fileId);
            collector.methodId = Long.parseLong(methodId);
            // System.out.println(collector);
            this.generateInputForOreo(collector);
            this.generateInputForScc(collector);
        }

    }

    public void generateInputForOreo(MetricCollector collector) {
        String internalSeparator = ",";
        String externalSeparator = "@#@";
        StringBuilder metricString = new StringBuilder("");
        StringBuilder metadataString = new StringBuilder("");
        StringBuilder actionTokenString = new StringBuilder("");

        StringBuilder stopwordsActionTokenString = new StringBuilder("");
        String methodNameActionString = "";
        metadataString.append(collector._path.getParent().getFileName()).append(internalSeparator)
                .append(collector._path.getFileName()).append(internalSeparator).append(collector.START_LINE)
                .append(internalSeparator).append(collector.END_LINE).append(internalSeparator)
                .append(collector._methodName).append(internalSeparator).append(collector.NTOKENS)
                .append(internalSeparator).append(collector.tokensMap.size()).append(internalSeparator)
                .append(collector.metricHash).append(internalSeparator).append(collector.fileId)
                .append(internalSeparator).append(collector.methodId);
        metricString.append(collector.COMP).append(internalSeparator).append(collector.NOS).append(internalSeparator)
                .append(collector.HVOC).append(internalSeparator).append(collector.HEFF).append(internalSeparator)
                .append(collector.CREF).append(internalSeparator).append(collector.XMET).append(internalSeparator)
                .append(collector.LMET).append(internalSeparator).append(collector.NOA).append(internalSeparator)
                .append(collector.HDIF).append(internalSeparator).append(collector.VDEC).append(internalSeparator)
                .append(collector.EXCT).append(internalSeparator).append(collector.EXCR).append(internalSeparator)
                .append(collector.CAST).append(internalSeparator).append(collector.NAND).append(internalSeparator)
                .append(collector.VREF).append(internalSeparator).append(collector.NOPR).append(internalSeparator)
                .append(collector.MDN).append(internalSeparator).append(collector.NEXP).append(internalSeparator)
                .append(collector.LOOP).append(internalSeparator).append(collector.NBLTRL).append(internalSeparator)
                .append(collector.NCLTRL).append(internalSeparator).append(collector.NNLTRL).append(internalSeparator)
                .append(collector.NNULLTRL).append(internalSeparator).append(collector.NSLTRL);
        String sep = "";
        for (Entry<String, Integer> entry : collector.mapMethodCallActionTokens.entrySet()) {
            actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
            sep = ",";
        }
        for (Entry<String, Integer> entry : collector.mapFieldAccessActionTokens.entrySet()) {
            actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
            sep = ",";
        }
        for (Entry<String, Integer> entry : collector.mapArrayAccessActionTokens.entrySet()) {
            actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
            sep = ",";
        }
        for (Entry<String, Integer> entry : collector.mapArrayBinaryAccessActionTokens.entrySet()) {
            actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
            sep = ",";
        }
        sep = "";
        for (Entry<String, Integer> entry : collector.mapStopWordsActionTokens.entrySet()) {
            stopwordsActionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
            sep = ",";
        }
        methodNameActionString = "_@" + collector._methodName + "@_:1";
        StringBuilder line = new StringBuilder("");
        line.append(metadataString).append(externalSeparator).append(metricString).append(externalSeparator)
                .append(actionTokenString).append(externalSeparator).append(stopwordsActionTokenString)
                .append(externalSeparator).append(methodNameActionString).append(externalSeparator)
                .append(collector.SLOC).append(System.lineSeparator());
        try {
            FileWriters.metricsFileWriter.append(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateInputForScc(MetricCollector collector) {
        String internalSeparator = ",";
        String externalSeparator = "@#@";
        StringBuilder metadataString = new StringBuilder("");
        StringBuilder tokenString = new StringBuilder("");

        metadataString.append(collector.fileId).append(internalSeparator).append(collector.methodId)
                .append(internalSeparator).append(MapUtils.addValues(collector.mapSccTokens)).append(internalSeparator)
                .append(collector.mapSccTokens.size());
        String sep = "";
        for (Entry<String, Integer> entry : collector.mapSccTokens.entrySet()) {
            String s = sep + entry.getKey() + "@@::@@" + entry.getValue();
            tokenString.append(s);
            sep = ",";
        }
        StringBuilder line = new StringBuilder("");
        line.append(metadataString).append(externalSeparator).append(tokenString).append(System.lineSeparator());
        try {
            FileWriters.bookkeepingWriter.append(collector.fileId + ":" + collector._path.toString() + ";"
                    + collector.methodId + ":" + collector._methodName + ";" + collector.START_LINE + ":"
                    + collector.END_LINE + System.lineSeparator());
            FileWriters.tokensFileWriter.append(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
