package uci.mondego;

public class MetricCollector {
    public String methodName;
    public int startLine;
    public int endLine;
    public int NOS;
    public int LOOP;
    public int numIf;
    public int NEXP;
    @Override
    public String toString() {
        return "MetricCollector [methodName=" + methodName + ", startLine=" + startLine + ", endLine=" + endLine
                + ", NOS=" + NOS + ", LOOP=" + LOOP + ", numIf=" + numIf + ", NEXP=" + NEXP + "]";
    }
}
