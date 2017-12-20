package uci.mondego;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class ZipInputProcessor implements IInputProcessor {
    public static String rootDirForInput = "/home/saini/Documents";
    public static String zipFile = rootDirForInput + File.separator + "test.zip";

    public void processInput(String filename) throws FileNotFoundException {
        try {
            ZipFile zipFile = new ZipFile(filename);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            BufferedReader br = null;
            String ext = null;
            StringBuilder sb = new StringBuilder();
            while (entries.hasMoreElements()) {
                ZipEntry currentEntry = entries.nextElement();
                ext = FilenameUtils.getExtension(currentEntry.getName());
                if (ext.equals("java")) {
                    Path p = Paths.get(currentEntry.getName());
                    // Read directly from zip
                    System.out.println("processing " + currentEntry.getName());
                    br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(currentEntry)));
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
            }
        } catch (IOException e1) {
            System.out.println("WARN: unknown error, skippig zip: " + filename);
            e1.printStackTrace();
        } catch (IllegalArgumentException e1){
            System.out.println("WARN: unknown error, skippig zip: " + filename);
            e1.printStackTrace();
        } catch (Exception e1){
            System.out.println("WARN: unknown error, skippig zip: " + filename);
            e1.printStackTrace();
        }
    }

    private void metricalize(String fileContent, Path path) throws FileNotFoundException {
        JavaMetricParser.FILE_COUNTER++;
        CompilationUnit cu = JavaParser.parse(fileContent);
        TreeVisitor visitor = new CustomVisitor(fileContent, path);
        visitor.visitPreOrder(cu);
    }

    public static void main(String[] args) throws FileNotFoundException {
        IInputProcessor processor = new ZipInputProcessor();
        processor.processInput(ZipInputProcessor.zipFile);
    }

}
