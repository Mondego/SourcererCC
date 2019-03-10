package com.mondego.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestGson {
    public Map<String, String> idToCodeMap;
    private String sep = "##\\+@\\+##";
    private String lineSep = "####@@@@####@@@@####";
    String codeFilename = "input/allcode/allcode.txt";

    public void populateMap() {
        this.idToCodeMap = new HashMap<String, String>();
        Scanner scan = null;
        try {
            scan = new Scanner(new File(this.codeFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  
        scan.useDelimiter("\\Z");  
        String content = scan.next(); 
        System.out.println(content.length());
        String lines[] = content.split(this.lineSep);
        System.out.println("num lines: "+ lines.length);
        try {
            for(String line : lines){
                String[] parts = line.split(this.sep);
                System.out.println("key: " + parts[0].trim());
                this.idToCodeMap.put(parts[0].trim(), parts[1]);
            }
        } finally {
            scan.close();
        }
    }

    public static void main(String[] args){
        TestGson testGson = new TestGson();
        testGson.populateMap();
        System.out.println(testGson.idToCodeMap.get("10050"));
    }
}
