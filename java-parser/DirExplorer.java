import java.io.File;
import java.util.Vector;

public class DirExplorer {

    public Vector<File>finder(String dirName){
        Vector<File> results = new Vector();

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

    public static void main(String[] args) {
        Vector<File> d = new DirExplorer().finder(".");

        for (File f : d)
            System.out.println(f.getName());
    }

}

