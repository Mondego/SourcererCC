/**
 * 
 */
package com.mondego.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * @author vaibhavsaini
 * 
 */
public class Tokenizer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("READING FILE");
        File f = new File("Test.java");
        try {
            String input = FileUtils.readFileToString(
                    f, "utf-8");
            for(String token :Tokenizer.processMethodBody(input)){
                System.out.println(token);
            }
            //System.out.println(input);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //String input = "0:main:/**      * @param args     \n*/     public static void main(String[] args) {         // TODO Auto-generated method stub \n         Set<Bag> setA = CloneTestHelper.getTestSet(1, 11);         Set<Bag> setB = CloneTestHelper.getTestSet(11, 21);         PrintWriter projectAWriter = null;         PrintWriter projectBWriter = null;         CloneHelper cloneHelper = new CloneHelper();         try {             File f = new File(projectA.txt);             if(f.delete()){                 System.out.println(deleted existing projectA.txt);             }             f = new File(projectB.txt);             if(f.delete()){                 System.out.println(deleted existing projectB.txt);             }             projectAWriter = Util.openFile(projectA.txt);             Util.writeToFile(projectAWriter, cloneHelper.stringify(setA), true);             projectBWriter = Util.openFile(projectB.txt);             Util.writeToFile(projectBWriter, cloneHelper.stringify(setB), true);";
        
        /*
         * input = t.replacePatter1(input); input = t.handleOps(input); input =
         * t.handleNoiseCharacters(input); //System.out.println(input); String[]
         * tokens = t.tokenize(input); ArrayList<String> s = new
         * ArrayList<String>(Arrays.asList(tokens)); System.out.println(s);
         */
    }

    public static List<String> processMethodBody(String input) {
        input = removeComments(input);
        //System.out.println("after removing comments: "+ input);
        input = replacePatter1(input);
        //System.out.println("after removing patter1: "+ input);
        input = handleOps(input);
        //System.out.println("after removing handleOps: "+ input);
        input = handleNoiseCharacters(input);
        //System.out.println("after removing noise: "+ input);
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
        //System.out.println("x: "+ x);
        x = x.replaceAll("\\n|\\r|\\r\\n", " ");
        //System.out.println("x2: "+ x);
        //String regexPattern = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";
        String regexPattern = "/\\*(?:.|[\\n\\r])*?\\*/";
                
              //  System.out.println(sourcecode.replaceAll(“/\\*(?:.|[\\n\\r])*?\\*/”,””));
        
        // String regexEnd = "*/";
        x = x.replaceAll(regexPattern, "");
      //  System.out.println("x3: "+ x);
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
        //System.out.println("input before: "+ input);
        String regexPattern = ";|@@::@@|@#@|@|#|\\$|~|`";
        String x = input.replaceAll(regexPattern, "");
        //System.out.println("input after: "+ x);
        return x;
    }

}
