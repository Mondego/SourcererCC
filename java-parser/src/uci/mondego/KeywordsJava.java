package uci.mondego;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeywordsJava {
 public static Set<String>reserved = new HashSet<String>(Arrays.asList(
         "abstract","continue","for","new","switch","assert","default",
         "goto","package","synchronized","boolean","do","if","private",
         "this","break","double","implements","protected","throw","byte",
         "else","import","public","throws","case","enum","instanceof","return",
         "transient","catch","extends","int","short","try","char","final","interface",
         "static","void","class","finally","long","strictfp","volatile","const","float",
         "native","super","while"));
 public static Set<String>operators = new HashSet<String>(Arrays.asList(
         "new","assert", "return"));
 
}
