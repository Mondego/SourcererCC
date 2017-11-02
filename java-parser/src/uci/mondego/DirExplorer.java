package uci.mondego;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirExplorer {

    public static void main(String[] args) {
        String path = "java_samples";
        System.out.println("Searching on " + path + " ...");

        List<File> d = finder(path);

        for (File f : d)
            System.out.println("   " + f.getName());
    }

    public static List<File> finder(String dirName) {
        List<File> results = new ArrayList<File>();

        File dir = new File(dirName);

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    results.add(file);
                }
            }
            if (file.isDirectory()) {
                results.addAll(finder(file.getName()));
            }
        }

        return results;
    }

}
