package uci.mondego;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MetricCollector {
    public File _file; // file object
    public String _methodName; // method name
    public Map<String, Integer> arrayAccessActionTokensMap = new HashMap<String, Integer>();
    public Map<String, Integer> arrayBinaryAccessActionTokensMap = new HashMap<String, Integer>();
    public int CAST; // number of class casts (Integer)x
    public int COMP; // McCabes cyclomatic complexity
    public int CREF; // Number of classes referenced
    public int END_LINE; // end line of this method
    public int EXCR; // Number of exceptions referenced
    public int EXCT; // Number of exceptions thrown
    public Map<String, Integer> fieldAccessActionTokensMap = new HashMap<String, Integer>();
    public int HBUG = 0;// Halstead prediction of number of bugs
    public int HDIF; // Halstead difficulty to implement a method
    public double HEFF; // Halstead effort to implement a method
    public int HLTH; // halstead length. not part of features
    public int HVOC; // Halstead vocabulary of a method
    public double HVOL; // halstead volumn. not part of features
    public Map<String, Integer> innerMethodParametersMap = new HashMap<String, Integer>();
    public Map<String, Integer> innerMethodsMap = new HashMap<String, Integer>();
    public Map<String, Integer> literalsMap = new HashMap<String, Integer>();
    public int LMET; // local methods called by method
    public int LOOP; // number of loops
    public int MDN; // Maximum depth of nesting in a method. Maximum depth of
                    // number of methods defined inside this method.
    public Map<String, Integer> methodCallActionTokensMap = new HashMap<String, Integer>();
    public int MOD = 0;// Number of modifiers
    public int NAND; // number of operands
    // http://www.verifysoft.com/en_halstead_metrics.html
    /*
     * Tokens of the following categories are all counted as operants by CMT++:
     * 1) IDENTIFIER all identifiers that are not reserved words.; 2) TYPENAME.;
     * TYPESPEC (type specifiers) Reserved words that specify type: bool, char,
     * double, float, int, long, short, signed, unsigned, void. This class also
     * includes some compiler specific nonstandard keywords.; CONSTANT
     * Character, numeric or string constants.
     */
    public int NEXP; // number of expressions
    public int NIF; // number of if statements
    public int NLOC = 0;// Number of lines code
    public int NOA; // number of arguments
    public int NOC = 0;// Number of comments
    public int NOCL = 0;// number of comment lines
    public int NOPR; // number of operators
    public int NOS; // number of statements
    public int NTOKENS; // number of tokens
    public Map<String, Integer> operandsMap = new HashMap<String, Integer>();
    public Map<String, Integer> operatorsMap = new HashMap<String, Integer>();
    public Map<String, Integer> parameterMap = new HashMap<String, Integer>();
    public Map<String, Integer> removeFromOperandsMap = new HashMap<String, Integer>();
    String simpleUniqueName = "simpleUniqueName";
    public int START_LINE; // start line of this method
    public int TDN; // total depth of nesting. Total number of methods defined
                    // inside this method
    public Map<String, Integer> tokensMap = new HashMap<String, Integer>();
    public Map<String, Integer> typeMap = new HashMap<String, Integer>();
    public int UNAND; // unique number of operands
    String uniqueName = "uniqueName";
    public int UNPOR; // unique number of operators
    public Map<String, Integer> variableDeclaredMap = new HashMap<String, Integer>();
    public Map<String, Integer> variablesRefMap = new HashMap<String, Integer>();
    public int VDEC; // Number of variables declared
    public int VREF; // number of variables referenced
    public int XMET; // number of external methods called by the method
    public String metricHash;
    public String tokenHash;
    public long fileId;
    public long methodId;

    public void addFieldAccessActionTokens(String fieldAccessString) {
        String[] tokens = fieldAccessString.split("\\.");
        if (Character.isUpperCase(tokens[0].charAt(0))) {
            this.CREF++; // heuristic to add class refs like System
            MapUtils.addOrUpdateMap(this.typeMap, tokens[0]);
        }
        // this.NOPR = this.NOPR + tokens.length-1; // accounting for "."
        // operators

        if (tokens.length > 1) {
            for (int i = 1; i < tokens.length; i++) {
                MapUtils.addOrUpdateMap(this.fieldAccessActionTokensMap, tokens[i]);
            }
        }
    }

    public void addMethodCallActionToken(String token) {
        MapUtils.addOrUpdateMap(this.methodCallActionTokensMap, token + "()");
        MapUtils.addOrUpdateMap(this.removeFromOperandsMap, token);
        MapUtils.addOrUpdateMap(this.operatorsMap, token);
        this.NOPR++; // accounting for functionCall Operator
    }

    public void addToken(String token) {
        token = this.strip(token);
        token = this.handleNoiseCharacters(token);
        token = this.removeNewLines(token);
        token = token.trim();
        if (token.length() > 0) {
            
            MapUtils.addOrUpdateMap(this.tokensMap, token);
            if (KeywordsJava.operators.contains(token)) {
                MapUtils.addOrUpdateMap(this.operatorsMap, token);
                this.NOPR++;
            } else {
                MapUtils.addOrUpdateMap(this.operandsMap, token);
            }
            this.NTOKENS++;
        }
    }
    
    private String strip(String str) {
        return str.replaceAll("(\'|\"|\\\\|:)", "");
    }
    private String handleNoiseCharacters(String input) {
        String regexPattern = ";|@@::@@|@#@|@|#";
        String x = input.replaceAll(regexPattern, "");
        return x;
    }
    private static String removeNewLines(String input) {
        String regexPatter = "\\n|\\r|\\r\\n";
        input = input.replaceAll(regexPatter, " ");
        return input;
    }

    public void computeHalsteadMetrics() {
        // http://www.virtualmachinery.com/sidebar2.htm
        MapUtils.subtractMaps(this.operandsMap, this.removeFromOperandsMap);
        // (List<String>) CollectionUtils.subtract(this.operands ,
        // this.removeFromOperands);
        this.NAND = MapUtils.addValues(this.operandsMap);
        this.UNAND = this.operandsMap.size();
        this.UNPOR = this.operatorsMap.size();
        this.setHLTH();
        this.setHVOC();
        this.setHDIF();
        this.setHVOL();
        this.setHEFF();
    }

    public void incCOMPCount() {
        this.COMP++;
    }

    public void incLoopCount() {
        this.LOOP++;
        this.incCOMPCount();
    }

    public void incNIFCount() {
        this.NIF++;
        this.incCOMPCount();
    }

    public void populateVariableRefList() {
        this.variablesRefMap = (Map<String, Integer>) ((HashMap<String, Integer>) this.operandsMap).clone();
        MapUtils.subtractMaps(this.variablesRefMap, this.variableDeclaredMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.typeMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.literalsMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.fieldAccessActionTokensMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.parameterMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.innerMethodsMap);
        MapUtils.subtractMaps(this.variablesRefMap, this.innerMethodParametersMap);
        // this.variablesRefList =(List<String>)
        // CollectionUtils.subtract(this.variablesRefList ,
        // KeywordsJava.reserved);
        for (String key : KeywordsJava.reserved) {
            this.variablesRefMap.remove(key);
        }
        this.VREF = MapUtils.addValues(this.variablesRefMap);
    }

    private void setHDIF() {
        this.HDIF = (this.UNPOR / 2) * (this.NAND / this.UNAND);
    }

    private void setHEFF() {
        this.HEFF = this.HDIF * this.HVOL;
    }

    private void setHLTH() {
        this.HLTH = this.NAND + this.NOPR;
    }

    private void setHVOC() {
        this.HVOC = this.UNAND + this.UNPOR;
    }

    private void setHVOL() {
        this.HVOL = this.HLTH * (Math.log(this.HVOC) / Math.log(2)); // logb(n)
                                                                     // =
                                                                     // loge(n)
                                                                     // /
                                                                     // loge(b)
    }
    // subtracts map 2 from map 1

    @Override
    public String toString() {
        return "MetricCollector [_file=" + _file + ", _methodName=" + _methodName + ", arrayAccessActionTokensMap="
                + arrayAccessActionTokensMap + ", arrayBinaryAccessActionTokensMap=" + arrayBinaryAccessActionTokensMap
                + ", CAST=" + CAST + ", COMP=" + COMP + ", CREF=" + CREF + ", END_LINE=" + END_LINE + ", EXCR=" + EXCR
                + ", EXCT=" + EXCT + ", fieldAccessActionTokensMap=" + fieldAccessActionTokensMap + ", HBUG=" + HBUG
                + ", HDIF=" + HDIF + ", HEFF=" + HEFF + ", HLTH=" + HLTH + ", HVOC=" + HVOC + ", HVOL=" + HVOL
                + ", innerMethodParametersMap=" + innerMethodParametersMap + ", innerMethodsMap=" + innerMethodsMap
                + ", literalsMap=" + literalsMap + ", LMET=" + LMET + ", LOOP=" + LOOP + ", MDN=" + MDN
                + ", methodCallActionTokensMap=" + methodCallActionTokensMap + ", MOD=" + MOD + ", NAND=" + NAND
                + ", NEXP=" + NEXP + ", NIF=" + NIF + ", NLOC=" + NLOC + ", NOA=" + NOA + ", NOC=" + NOC + ", NOCL="
                + NOCL + ", NOPR=" + NOPR + ", NOS=" + NOS + ", NTOKENS=" + NTOKENS + ", operandsMap=" + operandsMap
                + ", operatorsMap=" + operatorsMap + ", parameterMap=" + parameterMap + ", removeFromOperandsMap="
                + removeFromOperandsMap + ", simpleUniqueName=" + simpleUniqueName + ", START_LINE=" + START_LINE
                + ", TDN=" + TDN + ", tokensMap=" + tokensMap + ", typeMap=" + typeMap + ", UNAND=" + UNAND
                + ", uniqueName=" + uniqueName + ", UNPOR=" + UNPOR + ", variableDeclaredMap=" + variableDeclaredMap
                + ", variablesRefMap=" + variablesRefMap + ", VDEC=" + VDEC + ", VREF=" + VREF + ", XMET=" + XMET + "]";
    }

    public void populateMetricHash() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        messageDigest.reset();
        StringBuilder sb = new StringBuilder("");
        sb.append(this.COMP).append(this.NOS).append(this.HVOC).append(this.HEFF).append(this.CREF).append(this.XMET)
                .append(this.LMET).append(this.NOA).append(this.HDIF).append(this.VDEC).append(this.EXCT)
                .append(this.EXCR).append(this.CAST).append(this.NAND).append(this.VREF).append(this.NOPR)
                .append(this.MDN).append(this.NEXP).append(this.LOOP);

        messageDigest.update(sb.toString().getBytes(Charset.forName("UTF8")));
        this.metricHash = new String(messageDigest.digest());
    }

    public void populateTokenHash() {

    }

}
