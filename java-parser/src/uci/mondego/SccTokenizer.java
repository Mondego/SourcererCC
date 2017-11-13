package uci.mondego;

import java.util.ArrayList;
import java.util.List;

public class SccTokenizer {
    public static List<String> processString(String input) {
        input = removeComments(input);
        input = replacePatter1(input);
        input = handleOps(input);
        input = handleNoiseCharacters(input);
        // System.out.println(input);
        String[] tokens = tokenize(input);
        List<String> s = stripTokens(tokens);
        return s;
    }
    private static String strip(String str) {
        return str.replaceAll("(\'|\"|\\\\|:)", "");
    }
    
    private static List<String> stripTokens(String[] tokens){
        List<String> retTokens = new ArrayList<String>();
        for(String token : tokens){
            retTokens.add(strip(token));
        }
        return retTokens;
    }

    private static String handleOps(String input) {
        input = handleSimpleAssignmentOperator(input);
        input = handleArithmeticOperator(input);
        input = handleUnaryOperator(input);
        input = handleConditionalOperator(input);
        input = handleBitwiseOperator(input);
        return input;
    }

    private static String[] tokenize(String input) {
        String regex = "\\s+";
        String[] tokens = input.split(regex);
        return tokens;
    }

    private static String removeComments(String input) {
        String regexLineComment = "//.*(\\n|\\r|\\r\\n)";
        String x = input.replaceAll(regexLineComment, " ");
        x = x.replaceAll("\\n|\\r|\\r\\n", " ");
        String regexPattern = "/\\*.*\\*/";
        // String regexEnd = "*/";
        x = x.replaceAll(regexPattern, "");
        return x;
    }

    private static String replacePatter1(String input) {
        String regexPattern = ",|\\(|\\)|\\{|\\}|\\[|\\]|<|>";
        // String regexEnd = "*/";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }
    private static String handleSimpleAssignmentOperator(String input) {
        String regexPattern = "=|\\.";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }

    private static String handleArithmeticOperator(String input) {
        String regexPattern = "\\+|-|\\*|/|%";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }

    private static String handleUnaryOperator(String input) {
        String regexPattern = "!";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }

    private static String handleConditionalOperator(String input) {
        String regexPattern = "\\?";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }

    private static String handleBitwiseOperator(String input) {
        String regexPattern = "&|\\^|\\|";
        String x = input.replaceAll(regexPattern, " ");
        return x;
    }

    private static String handleNoiseCharacters(String input) {
        String regexPattern = ";|@@::@@|@#@|@|#";
        String x = input.replaceAll(regexPattern, "");
        return x;
    }
}
