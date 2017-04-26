package preprocessing;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Farima on 4/26/2017.
 */
public class FileSorter {
    public static void main(String[] args) {
        try{
        String inputFileName=new File(Paths.get("./input_preprocessing/mode_sorting").toString()).listFiles()[0].getName();
        String ouputFileName=inputFileName.substring(0,inputFileName.indexOf("."))+"_sortedModes";

        String eachFile="";
        BufferedReader bf=new BufferedReader(new FileReader(Paths.get("./input_preprocessing/mode_sorting").toString()+"/"+inputFileName));
        PrintWriter writer=new PrintWriter((Paths.get("./output_preprocessing/mode_sorting").toString()+"/"+ouputFileName)+".txt");


        while ((eachFile=bf.readLine())!=null) {
            String[] separatedLineFromMeta = eachFile.split("@#@");
            if (Integer.parseInt(separatedLineFromMeta[0].split(",")[2]) >=65&&Integer.parseInt(separatedLineFromMeta[0].split(",")[2]) <=500000) {//we do calculations just for files have more than 65 tokens and less than 5000 tokens
                HashMap<String,Integer> frequencies=new HashMap<>();
                HashMap<String,Integer> ids=new HashMap<>();
                String[] words = separatedLineFromMeta[1].split(",");
                String word = "";
                String lineNewFile = "";
                for (int i = 0; i < words.length; i++) {
                    word = words[i].split("@@::@@")[0];

                    Integer frequency = Integer.valueOf(words[i].split("@@::@@")[1]);
                    Integer id = Integer.valueOf(words[i].split("@@::@@")[2]);
                    frequencies.put(word,frequency);
                    ids.put(word,id);
                }
                Integer[] frequenciesList=Arrays.copyOf(frequencies.values().toArray(),frequencies.values().toArray().length,Integer[].class);
                Arrays.sort(frequenciesList,Collections.reverseOrder());
                for (int i = 0; i <frequenciesList.length ; i++) {
                    for (String w:frequencies.keySet()){
                        if (frequencies.get(w)==frequenciesList[i]){
                            lineNewFile +=w + "@@::@@" + frequenciesList[i] + "@@::@@" +ids.get(w)+",";
                            frequencies.remove(w);
                            ids.remove(w);
                            break;
                        }
                    }
                }

                writer.append(separatedLineFromMeta[0] + "@#@" + lineNewFile.substring(0,lineNewFile.length()-1) + System.lineSeparator());
            }
        }
        writer.close();
        bf.close();

    }

        catch (IOException e)
    {
        e.printStackTrace();
    }
    }
}
