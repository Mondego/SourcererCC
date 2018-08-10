package uci.mondego;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricCollector {
    public File _file; // file object
    public Path _path; // path of the file. useful when file is inside a tgz or zip
    public String _methodName; // method name
    public int CAST; // number of class casts (Integer)x
    public int COMP; // McCabes cyclomatic complexity
    public int CREF; // Number of classes referenced
    public int END_LINE; // end line of this method
    public int EXCR; // Number of exceptions referenced
    public int EXCT; // Number of exceptions thrown
    public long fileId;
    public int HBUG = 0;// Halstead prediction of number of bugs
    public int HDIF; // Halstead difficulty to implement a method
    public double HEFF; // Halstead effort to implement a method
    public int HLTH; // halstead length. not part of features
    public int HVOC; // Halstead vocabulary of a method
    public double HVOL; // halstead volumn. not part of features
    public int LMET; // local methods called by method
    public int LOOP; // number of loops
    public Map<String, Integer> mapArrayAccessActionTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapArrayBinaryAccessActionTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapFieldAccessActionTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapInnerMethodParameters = new HashMap<String, Integer>();
    public Map<String, Integer> mapInnerMethods = new HashMap<String, Integer>();
    public Map<String, Integer> mapLiterals = new HashMap<String, Integer>();
                    // number of methods defined inside this method.
    public Map<String, Integer> mapMethodCallActionTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapOperands = new HashMap<String, Integer>();
    public Map<String, Integer> mapOperators = new HashMap<String, Integer>();
    public Map<String, Integer> mapParameter = new HashMap<String, Integer>();
    public Map<String, Integer> mapRemoveFromOperands = new HashMap<String, Integer>();
    public Map<String, Integer> mapSccTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapStopWordsActionTokens = new HashMap<String, Integer>();
    public Map<String, Integer> mapVariableDeclared = new HashMap<String, Integer>();
    public Map<String, Integer> mapVariablesRef = new HashMap<String, Integer>();
    public int MDN; // Maximum depth of nesting in a method. Maximum depth of
    public long methodId;
    public String metricHash;
    public int MOD = 0;// Number of modifiers
    public int NAND; // number of operands
    public int NBLTRL; // number of boolean literals
    public int NCLTRL; // number of char literals
    public int SLOC;
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
    public int NNLTRL; // number of number literals: integer literals, double literals, long literals
                    public int NNULLTRL; // number of NULL literals
    public int NOA; // number of arguments
    public int NOC = 0;// Number of comments
    public int NOCL = 0;// number of comment lines
    public int NOPR; // number of operators
    public int NOS; // number of statements
    public int NSLTRL; // number of string literals
    public int NTOKENS; // number of tokens
    String simpleUniqueName = "simpleUniqueName";
    public int START_LINE; // start line of this method
    public int TDN; // total depth of nesting. Total number of methods defined
    public String tokenHash;
    // inside this method
    public Map<String, Integer> tokensMap = new HashMap<String, Integer>();
    public Map<String, Integer> typeMap = new HashMap<String, Integer>();
    public int UNAND; // unique number of operands
    String uniqueName = "uniqueName";
    public int UNPOR; // unique number of operators
    public int VDEC; // Number of variables declared
    public int VREF; // number of variables referenced
    public int XMET; // number of external methods called by the method

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
                MapUtils.addOrUpdateMap(this.mapFieldAccessActionTokens, tokens[i]);
            }
        }
    }

    public void addMethodCallActionToken(String token) {
        if(Util.stopwordsActionTokens.contains(token+"()")){
            MapUtils.addOrUpdateMap(this.mapStopWordsActionTokens, token+"()");
        }else{
            MapUtils.addOrUpdateMap(this.mapMethodCallActionTokens, token + "()");
        }
        MapUtils.addOrUpdateMap(this.mapRemoveFromOperands, token);
        MapUtils.addOrUpdateMap(this.mapOperators, token);
        this.NOPR++; // accounting for functionCall Operator
    }

    public void addToken(String token) {
        token = token.trim();
        if (token.length() > 0) {
            
            MapUtils.addOrUpdateMap(this.tokensMap, token);
            if (KeywordsJava.operators.contains(token)) {
                MapUtils.addOrUpdateMap(this.mapOperators, token);
                this.NOPR++;
            } else {
                MapUtils.addOrUpdateMap(this.mapOperands, token);
            }
            this.NTOKENS++;
        }
        List<String> sccTokens = SccTokenizer.processString(token);
        for(String sccToken : sccTokens){
            if(sccToken.length()>0){
                MapUtils.addOrUpdateMap(this.mapSccTokens, sccToken);
            }
        }
    }

    public void computeHalsteadMetrics() {
        // http://www.virtualmachinery.com/sidebar2.htm
        MapUtils.subtractMaps(this.mapOperands, this.mapRemoveFromOperands);
        // (List<String>) CollectionUtils.subtract(this.operands ,
        // this.removeFromOperands);
        this.NAND = MapUtils.addValues(this.mapOperands);
        this.UNAND = this.mapOperands.size();
        this.UNPOR = this.mapOperators.size();
        this.setHLTH();
        this.setHVOC();
        this.setHDIF();
        this.setHVOL();
        this.setHEFF();
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
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

        messageDigest.update(sb.toString().getBytes(Charset.forName("UTF-8")));
        this.metricHash = this.convertByteArrayToHexString(messageDigest.digest());
    }

    public void populateTokenHash() {

    }

    public void populateVariableRefList() {
        this.mapVariablesRef = (Map<String, Integer>) ((HashMap<String, Integer>) this.mapOperands).clone();
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapVariableDeclared);
        MapUtils.subtractMaps(this.mapVariablesRef, this.typeMap);
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapLiterals);
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapFieldAccessActionTokens);
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapParameter);
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapInnerMethods);
        MapUtils.subtractMaps(this.mapVariablesRef, this.mapInnerMethodParameters);
        // this.variablesRefList =(List<String>)
        // CollectionUtils.subtract(this.variablesRefList ,
        // KeywordsJava.reserved);
        for (String key : KeywordsJava.reserved) {
            this.mapVariablesRef.remove(key);
        }
        this.VREF = MapUtils.addValues(this.mapVariablesRef);
    }

    private void setHDIF() {
        try{
            this.HDIF = (this.UNPOR / 2) * (this.NAND / this.UNAND);
        }catch(ArithmeticException e){
            // ignore
        }
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
        return "MetricCollector [_path=" + _path + ", _methodName=" + _methodName + ", CAST=" + CAST + ", COMP=" + COMP
                + ", CREF=" + CREF + ", END_LINE=" + END_LINE + ", EXCR=" + EXCR + ", EXCT=" + EXCT + ", fileId="
                + fileId + ", HBUG=" + HBUG + ", HDIF=" + HDIF + ", HEFF=" + HEFF + ", HLTH=" + HLTH + ", HVOC=" + HVOC
                + ", HVOL=" + HVOL + ", LMET=" + LMET + ", LOOP=" + LOOP + ", mapArrayAccessActionTokens="
                + mapArrayAccessActionTokens + ", mapArrayBinaryAccessActionTokens=" + mapArrayBinaryAccessActionTokens
                + ", mapFieldAccessActionTokens=" + mapFieldAccessActionTokens + ", mapInnerMethodParameters="
                + mapInnerMethodParameters + ", mapInnerMethods=" + mapInnerMethods + ", mapLiterals=" + mapLiterals
                + ", mapMethodCallActionTokens=" + mapMethodCallActionTokens + ", mapOperands=" + mapOperands
                + ", mapOperators=" + mapOperators + ", mapParameter=" + mapParameter + ", mapRemoveFromOperands="
                + mapRemoveFromOperands + ", mapSccTokens=" + mapSccTokens + ", mapVariableDeclared="
                + mapVariableDeclared + ", mapVariablesRef=" + mapVariablesRef + ", MDN=" + MDN + ", methodId="
                + methodId + ", metricHash=" + metricHash + ", MOD=" + MOD + ", NAND=" + NAND + ", NBLTRL=" + NBLTRL
                + ", NCLTRL=" + NCLTRL + ", NEXP=" + NEXP + ", NIF=" + NIF + ", NLOC=" + NLOC + ", NNLTRL=" + NNLTRL
                + ", NNULLTRL=" + NNULLTRL + ", NOA=" + NOA + ", NOC=" + NOC + ", NOCL=" + NOCL + ", NOPR=" + NOPR
                + ", NOS=" + NOS + ", NSLTRL=" + NSLTRL + ", NTOKENS=" + NTOKENS + ", simpleUniqueName="
                + simpleUniqueName + ", START_LINE=" + START_LINE + ", TDN=" + TDN + ", tokenHash=" + tokenHash
                + ", tokensMap=" + tokensMap + ", typeMap=" + typeMap + ", UNAND=" + UNAND + ", uniqueName="
                + uniqueName + ", UNPOR=" + UNPOR + ", VDEC=" + VDEC + ", VREF=" + VREF + ", XMET=" + XMET + "]";
    }

}
