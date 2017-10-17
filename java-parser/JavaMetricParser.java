import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.JavaParser;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Vector;

public class JavaMetricParser {

	public void metricalize (File file) throws FileNotFoundException{
		System.out.println("Metricalizing "+file.getName());

		CompilationUnit cUnit = new JavaParser().parse(file);

		System.out.println("   with the imports: "+cUnit.getImports());
	}

	public static void main(String[] args) throws FileNotFoundException{
		JavaMetricParser mParser = new JavaMetricParser();
		Vector<File> files = new DirExplorer().finder("java_samples");

		for (File f : files){
			mParser.metricalize(f);
		}
	}

}
