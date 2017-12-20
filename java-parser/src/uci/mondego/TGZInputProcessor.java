package uci.mondego;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class TGZInputProcessor implements IInputProcessor {
    public static String rootDirForInput = "/Users/vaibhavsaini/Documents";
    public static String tgzFile = rootDirForInput + File.separator + "test.tgz";

    @Override
    public void processInput(String filename) throws FileNotFoundException {
        TarArchiveInputStream tarInput;
        try {
            tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(filename)));
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            BufferedReader br = null;
            String ext = null;
            StringBuilder sb = new StringBuilder();
            while (currentEntry != null) {
                ext = FilenameUtils.getExtension(currentEntry.getName());
                if (ext.equals("java")) {
                    Path p = Paths.get(currentEntry.getName());
                    br = new BufferedReader(new InputStreamReader(tarInput)); // Read
                                                                              // directly
                                                                              // from
                                                                              // tarInput
                    sb.setLength(0);
                    String line;
                    while ((line = br.readLine()) != null) {
                        // System.out.println(line);
                        sb.append(line).append(System.lineSeparator());
                    }
                    try {
                        this.metricalize(sb.toString(), p);
                    } catch (FileNotFoundException e) {
                        System.out.println("WARN: File not found, skipping file: " + p.toString());
                        try {
                            FileWriters.errorPw.write(p.toString() + System.lineSeparator());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (ParseProblemException e) {
                        System.out.println("WARN: parse problem exception, skippig file: " + p.toString());
                        try {
                            FileWriters.errorPw.write(p.toString() + System.lineSeparator());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("WARN: unknown error, skippig file: " + p.toString());
                        try {
                            FileWriters.errorPw.write(p.toString() + System.lineSeparator());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                    try {
                        br.close();
                    } catch (IOException er) {
                        System.out.println("WARN: " + er.getMessage());
                    }
                }

                currentEntry = tarInput.getNextTarEntry(); // iterate to the
                                                           // next file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void metricalize(String fileContent, Path path) throws FileNotFoundException {
        JavaMetricParser.FILE_COUNTER++;
        CompilationUnit cu = JavaParser.parse(fileContent);
        TreeVisitor visitor = new CustomVisitor(fileContent, path);
        visitor.visitPreOrder(cu);
    }

    public static void main(String[] args) throws FileNotFoundException {
        IInputProcessor processor = new TGZInputProcessor();
        processor.processInput(TGZInputProcessor.tgzFile);
    }

}
