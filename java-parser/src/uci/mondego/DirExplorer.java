package uci.mondego;

import java.io.File;
import java.util.Vector;

public class DirExplorer {

    public static void main(String[] args) {
        String path = "java_samples";
        System.out.println("Searching on "+path+" ...");

        Vector<File> d = new DirExplorer().finder(path);

        for (File f : d)
            System.out.println("   "+f.getName());
    }

    public Vector<File> finder(String dirName) {
        Vector<File> results = new Vector<File>();

        File dir = new File(dirName);

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    results.add(file);
                }
            }
            if (file.isDirectory()) {
                results.addAll(this.finder(file.getName()));
            }
        }

        return results;
    }

}


