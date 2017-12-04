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
                    br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(currentEntry)));
                    sb.setLength(0);
                    String line;
                    while ((line = br.readLine()) != null) {
                        // System.out.println(line);
                        sb.append(line).append(System.lineSeparator());
                    }
                    this.metricalize(sb.toString(), p);
                    try {
                        br.close();
                    } catch (IOException er) {
                        System.out.println("WARN: "+ er.getMessage());
                    }
                }
            }
        } catch (IOException e1) {
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
