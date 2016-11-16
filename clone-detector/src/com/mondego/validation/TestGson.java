package com.mondego.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TestGson {
    public Map<String,String> idToCodeMap;
    private String sep = "##\\+@\\+##";
    private String lineSep = "####@@@@####@@@@####";
    String codeFilename = "input/allcode/allcode.txt";
    //String testFile = "input/allcode/test.txt";
    public void populateMap(){
        
        this.idToCodeMap = new HashMap<String, String>();
        //System.out.println("reading file: " + this.codeFilename);
        Scanner scan = null;
        try {
            scan = new Scanner(new File(this.codeFilename));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  
        scan.useDelimiter("\\Z");  
        String content = scan.next(); 
        System.out.println(content.length());
        String lines[] = content.split(this.lineSep);
        System.out.println("num lines: "+ lines.length);
        try {
            for(String line : lines){
                //System.out.println("line is: "+line);
                String[] parts = line.split(this.sep);
                System.out.println("key: "+parts[0].trim());
                this.idToCodeMap.put(parts[0].trim(), parts[1]);
            }
        } finally {
            scan.close();
        }
    }
    
    
    private void test(){
        Gson gson =  new GsonBuilder().create();
        String json = gson.toJson(this.idToCodeMap);
        System.out.println(json);
        Type typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();
        Map<String, String> newMap = gson.fromJson(json, typeOfHashMap);
        System.out.println(newMap);
    }
    
    public static void main(String[] args){
        TestGson testGson = new TestGson();
        testGson.populateMap();
        //testGson.test();
        System.out.println(testGson.idToCodeMap.get("10050"));
    }
}
