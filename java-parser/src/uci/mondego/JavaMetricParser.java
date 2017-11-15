package uci.mondego;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
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
    public Writer metricsFileWriter = null;
    public Writer bookkeepingWriter = null;
    public Writer tokensFileWriter = null;
    public Writer errorPw = null;
    public String outputDirPath;
    public String metricsFileName;
    public String tokensFileName;
    public String bookkeepingFileName;
    public String errorFileName;
    public static String prefix;
    public static long FILE_COUNTER;
    public static long METHOD_COUNTER;

    public JavaMetricParser(String inputFilePath) {
        this.metricsFileName = "mlcc_input.file";
        this.bookkeepingFileName = "bookkeeping.file";
        this.errorFileName = "error_metrics.file";
        this.tokensFileName = "scc_input.file";
        JavaMetricParser.prefix = this.getBaseName(inputFilePath);
        this.outputDirPath = JavaMetricParser.prefix + "_metric_output";
        File outDir = new File(this.outputDirPath);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
    }

    private String getBaseName(String path) {
        File inputFile = new File(path);
        String fileName = inputFile.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length > 0) {
            String filename = args[0];
            JavaMetricParser javaMetricParser = new JavaMetricParser(filename);
            BufferedReader br;
            try {
                if (null == javaMetricParser.metricsFileWriter) {
                    javaMetricParser.metricsFileWriter = Util.openFile(
                            javaMetricParser.outputDirPath + File.separator + javaMetricParser.metricsFileName, false);

                    javaMetricParser.errorPw = Util.openFile(
                            javaMetricParser.outputDirPath + File.separator + javaMetricParser.errorFileName, false);
                    javaMetricParser.bookkeepingWriter = Util.openFile(
                            javaMetricParser.outputDirPath + File.separator + javaMetricParser.bookkeepingFileName,
                            false);
                    javaMetricParser.tokensFileWriter = Util.openFile(
                            javaMetricParser.outputDirPath + File.separator + javaMetricParser.tokensFileName, false);
                }
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null && line.trim().length() > 0) {
                    javaMetricParser.calculateMetrics(line.trim());
                }
                Util.closeOutputFile(javaMetricParser.metricsFileWriter);
                Util.closeOutputFile(javaMetricParser.errorPw);
                Util.closeOutputFile(javaMetricParser.tokensFileWriter);
                Util.closeOutputFile(javaMetricParser.bookkeepingWriter);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FATAL: please specify the file with list of directories!");
        }
        System.out.println("done!");
    }

    public void calculateMetrics(String filename) throws FileNotFoundException {
        System.out.println("processing directory: " + filename);
        List<File> files = DirExplorer.finder(filename);
        for (File f : files) {
            try {
                this.metricalize(f);
            } catch (FileNotFoundException e) {
                System.out.println("WARN: File not found, skipping file: " + f.getAbsolutePath());
                try {
                    this.errorPw.write(f.getAbsolutePath() + System.lineSeparator());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseProblemException e) {
                System.out.println("WARN: parse problem exception, skippig file: " + f.getAbsolutePath());
                try {
                    this.errorPw.write(f.getAbsolutePath() + System.lineSeparator());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("WARN: unknown error, skippig file: " + f.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    public void metricalize(final File file) throws FileNotFoundException {
        System.out.println("Metricalizing " + file.getName());
        JavaMetricParser.FILE_COUNTER++;
        CompilationUnit cu = JavaParser.parse(file);
        TreeVisitor astVisitor = new TreeVisitor() {

            @Override
            public void process(Node node) {
                if (node instanceof MethodDeclaration || node instanceof ConstructorDeclaration) {
                    JavaMetricParser.METHOD_COUNTER++;
                    MetricCollector collector = new MetricCollector();
                    collector._file = file;
                    for (Modifier c : ((MethodDeclaration) node).getModifiers()) {
                        collector.addToken(c.toString());
                        MapUtils.addOrUpdateMap(collector.mapRemoveFromOperands, c.toString());
                        collector.MOD++;
                    }
                    NodeList<ReferenceType> exceptionsThrown = ((MethodDeclaration) node).getThrownExceptions();
                    if (exceptionsThrown.size() > 0) {
                        collector.addToken("throws");
                    }
                    NodeList<Parameter> nl = ((MethodDeclaration) node).getParameters();
                    for (Parameter p : nl) {

                        for (Node c : p.getChildNodes()) {
                            if (c instanceof SimpleName)
                                MapUtils.addOrUpdateMap(collector.mapParameter, c.toString());
                        }
                    }
                    collector.NOA = nl.size();
                    collector.START_LINE = node.getBegin().get().line;
                    collector.END_LINE = node.getEnd().get().line;
                    collector._methodName = ((MethodDeclaration) node).getName().asString();
                    MapUtils.addOrUpdateMap(collector.mapRemoveFromOperands, collector._methodName);
                    node.accept(new MethodVisitor(), collector);
                    collector.computeHalsteadMetrics();
                    collector.COMP++; // add 1 for the default path.
                    collector.NOS++; // accounting for method declaration
                    collector.mapInnerMethods.remove(collector._methodName);
                    MapUtils.subtractMaps(collector.mapInnerMethodParameters, collector.mapParameter);
                    collector.populateVariableRefList();
                    collector.populateMetricHash();
                    String fileId = JavaMetricParser.prefix + JavaMetricParser.FILE_COUNTER;
                    String methodId = fileId + "00" + JavaMetricParser.METHOD_COUNTER;
                    // System.out.println("fileId is : " + fileId + ", methodId
                    // is: " + methodId);
                    collector.fileId = Long.parseLong(fileId);
                    collector.methodId = Long.parseLong(methodId);
                    System.out.println(collector);
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
                metadataString.append(collector._file.getParentFile().getName()).append(internalSeparator)
                        .append(collector._file.getName()).append(internalSeparator).append(collector.START_LINE)
                        .append(internalSeparator).append(collector.END_LINE).append(internalSeparator)
                        .append(collector._methodName).append(internalSeparator).append(collector.NTOKENS)
                        .append(internalSeparator).append(collector.tokensMap.size()).append(internalSeparator)
                        .append(collector.metricHash).append(internalSeparator).append(collector.fileId)
                        .append(internalSeparator).append(collector.methodId);
                metricString.append(collector.COMP).append(internalSeparator).append(collector.NOS)
                        .append(internalSeparator).append(collector.HVOC).append(internalSeparator)
                        .append(collector.HEFF).append(internalSeparator).append(collector.CREF)
                        .append(internalSeparator).append(collector.XMET).append(internalSeparator)
                        .append(collector.LMET).append(internalSeparator).append(collector.NOA)
                        .append(internalSeparator).append(collector.HDIF).append(internalSeparator)
                        .append(collector.VDEC).append(internalSeparator).append(collector.EXCT)
                        .append(internalSeparator).append(collector.EXCR).append(internalSeparator)
                        .append(collector.CAST).append(internalSeparator).append(collector.NAND)
                        .append(internalSeparator).append(collector.VREF).append(internalSeparator)
                        .append(collector.NOPR).append(internalSeparator).append(collector.MDN)
                        .append(internalSeparator).append(collector.NEXP).append(internalSeparator)
                        .append(collector.LOOP).append(internalSeparator).append(collector.NBLTRL)
                        .append(internalSeparator).append(collector.NCLTRL).append(internalSeparator)
                        .append(collector.NNLTRL).append(internalSeparator).append(collector.NNULLTRL)
                        .append(internalSeparator).append(collector.NSLTRL);
                String sep = "";
                for (Entry<String, Integer> entry : collector.mapMethodCallActionTokens.entrySet()) {
                    actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
                    sep = ",";
                }
                sep = "";
                for (Entry<String, Integer> entry : collector.mapFieldAccessActionTokens.entrySet()) {
                    actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
                    sep = ",";
                }
                sep = "";
                for (Entry<String, Integer> entry : collector.mapArrayAccessActionTokens.entrySet()) {
                    actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
                    sep = ",";
                }
                sep = "";
                for (Entry<String, Integer> entry : collector.mapArrayBinaryAccessActionTokens.entrySet()) {
                    actionTokenString.append(sep).append(entry.getKey() + ":" + entry.getValue());
                    sep = ",";
                }
                sep = "";
                for (Entry<String, Integer> entry : collector.mapStopWordsActionTokens.entrySet()) {
                    stopwordsActionTokenString.append(entry.getKey() + ":" + entry.getValue());
                    sep = ",";
                }
                methodNameActionString = "_@" + collector._methodName + "@_:1";
                StringBuilder line = new StringBuilder("");
                line.append(metadataString).append(externalSeparator).append(metricString).append(externalSeparator)
                        .append(actionTokenString).append(externalSeparator).append(stopwordsActionTokenString)
                        .append(externalSeparator).append(methodNameActionString).append(System.lineSeparator());
                try {
                    metricsFileWriter.append(line.toString());
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
                        .append(internalSeparator).append(MapUtils.addValues(collector.mapSccTokens))
                        .append(internalSeparator).append(collector.mapSccTokens.size());
                String sep = "";
                for (Entry<String, Integer> entry : collector.mapSccTokens.entrySet()) {
                    String s = sep + entry.getKey() + "@@::@@" + entry.getValue();
                    tokenString.append(s);
                    sep = ",";
                }
                StringBuilder line = new StringBuilder("");
                line.append(metadataString).append(externalSeparator).append(tokenString)
                        .append(System.lineSeparator());
                try {
                    bookkeepingWriter.append(collector.fileId + ":" + collector._file.getAbsolutePath() + ";"
                            + collector.methodId + ":" + collector._methodName + ";" + collector.START_LINE + ":"
                            + collector.END_LINE + System.lineSeparator());
                    tokensFileWriter.append(line.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        astVisitor.visitPreOrder(cu);
    }
}