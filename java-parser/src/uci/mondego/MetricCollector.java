package uci.mondego;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MetricCollector {
    public List<String> actionTokens;
    public int CAST; // number of class casts (Integer)x
    public int COMP; // McCabes cyclomatic complexity
    public int CREF; // Number of classes referenced
    public int END_LINE; // end line of this method
    public int EXCR; // Number of exceptions referenced
    public int EXCT; // Number of exceptions thrown
    public File file; // file object
    public int HDIF; // Halstead difficulty to implement a method
    public int HEFF; // Halstead effort to implement a method
    public int HVOC; // Halstead vocabulary of a method
    public int LMET; // local methods called by method
    public int LOOP; // number of loops
    public int MDN; // Maximum depth of nesting in a method
    public String methodName; // method name
    public int NAND; // number of operands
    //https://www.scribd.com/doc/99533/Halstead-s-Operators-and-Operands-in-C-C-JAVA-by-Indranil-Nandy
    public int NEXP; // number of expressions
    public int NOA; // number of arguments
    public int NOPR; // number of operators
    public int NOS; // number of statements
    public int NTOKENS; // number of tokens
    public int numIf; // number of if statements
    public int START_LINE; // start line of this method
    public int VDEC; // Number of variables declared
    public int VREF; // number of variables referenced
    public int XMET; // number of external methods called by the method
    public List<String> simpleNames = new ArrayList<String>();

    @Override
    public String toString() {
        return "MetricCollector [actionTokens=" + actionTokens + ", CAST=" + CAST + ", COMP=" + COMP + ", CREF=" + CREF
                + ", END_LINE=" + END_LINE + ", EXCR=" + EXCR + ", EXCT=" + EXCT + ", file=" + file + ", HDIF=" + HDIF
                + ", HEFF=" + HEFF + ", HVOC=" + HVOC + ", LMET=" + LMET + ", LOOP=" + LOOP + ", MDN=" + MDN
                + ", methodName=" + methodName + ", NAND=" + NAND + ", NEXP=" + NEXP + ", NOA=" + NOA + ", NOPR=" + NOPR
                + ", NOS=" + NOS + ", NTOKENS=" + NTOKENS + ", numIf=" + numIf + ", START_LINE=" + START_LINE
                + ", VDEC=" + VDEC + ", VREF=" + VREF + ", XMET=" + XMET + ", simpleNames=" + simpleNames + "]";
    }
}
