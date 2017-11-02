package uci.mondego;

import java.io.File;

public class MetricCollector {
    public File file; // file object
    public String methodName; // method name
    public int startLine; // start line of this method
    public int endLine; // end line of this method
    public int NOS; // number of statements
    public int LOOP; // number of loops
    public int numIf; // number of if statements
    public int NEXP; // number of expressions
    public int NOA; // number of arguments
    public int CAST; // number of class casts (Integer)x
    public int NOPR; // number of operators
    public int NAND; // number of operands
    @Override
    public String toString() {
        return "MetricCollector [file=" + file.getName() + ", methodName=" + methodName + ", startLine=" + startLine
                + ", endLine=" + endLine + ", NOS=" + NOS + ", LOOP=" + LOOP + ", numIf=" + numIf + ", NEXP=" + NEXP
                + ", NOA=" + NOA + ", CAST=" + CAST + ", NOPR=" + NOPR + ", NAND=" + NAND + "]";
    }
}
