package uci.mondego;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class Util {
    
    public static Set<String> stopwordsActionTokens;
    //public static String stopwordsActionTokensFilepath="/home/sourcerer/oreo_related/SourcererCC/java-parser/res/stopwordsActionTokens.txt";
    public static String stopwordsActionTokensFilepath="res/stopwordsActionTokens.txt";
    static{
        BufferedReader br;
        Util.stopwordsActionTokens = new HashSet<String>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(Util.stopwordsActionTokensFilepath), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                Util.stopwordsActionTokens.add(line.trim());
                System.out.println(line);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("Fatal error, exiting now");
            System.exit(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Fatal error, exiting now");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fatal error, exiting now");
            System.exit(1);
        }
        
    }
    public static Writer openFile(String filename, boolean append)
            throws IOException {
        try {
            Writer pWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename, append), "UTF-8"),1024*100);
            return pWriter;

        } catch (IOException e) {
            // IO exception caught
            System.err.println(e.getMessage());
            throw e;
        }
    }
    public static void closeOutputFile(Writer pWriter) {
        if (null != pWriter) {
            try {
                pWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                pWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
