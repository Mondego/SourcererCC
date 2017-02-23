package preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.math3.stat.StatUtils;
/**
 * Created by Farima 
 */
public class Preprocessor {
    public static void main(String[] args) {
        addStatsAndGiveIdsToTokens();
    }
    private static void addStatsAndGiveIdsToTokens(){
        int randomNumber=0;
        try {
            String eachFile="";
            BufferedReader bf=new BufferedReader(new FileReader("blocks.file"));
            PrintWriter writer=new PrintWriter("output.txt");
            HashMap<String,Integer> wordNumbers=new HashMap<>();
            while ((eachFile=bf.readLine())!=null){
                String[] separatedLineFromMeta=eachFile.split("@#@");
                String[] words=separatedLineFromMeta[1].split(",");
                String word="";
                String lineNewFile="";
                double mean;
                double variance;
                double mode;
                double median;
                ArrayList<Double> numbersInLine=new ArrayList<>();
                for (int i = 0; i <words.length ; i++) {
                    word=words[i].split("@@::@@")[0];
                    Integer frequency=Integer.valueOf(words[i].split("@@::@@")[1]);
                    if (!wordNumbers.containsKey(word)){
                        randomNumber++;
                        wordNumbers.put(word,randomNumber);
                    }
                    for (int j = 0; j <frequency ; j++) {
                        numbersInLine.add(Double.valueOf(wordNumbers.get(word)));
                    }

                    lineNewFile+=words[i]+"@@::@@"+wordNumbers.get(word)+",";
                }
                double[] numbers=new double[numbersInLine.size()];
                for (int i = 0; i <numbers.length ; i++) {
                    numbers[i]=numbersInLine.get(i);
                }
                median=getMedian(numbers);
                variance=StatUtils.variance(numbers);
                mean=StatUtils.mean(numbers);
                writer.append(separatedLineFromMeta[0]+","+median+","+mean+","+variance+"@#@"+lineNewFile.substring(0,lineNewFile.length()-1)+System.lineSeparator());
            }
            writer.close();
            bf.close();

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private static double getMedian(double[] numArray) {
        Arrays.sort(numArray);
        double median;
        if (numArray.length % 2 == 0)
            median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
        else
            median = (double) numArray[numArray.length/2];
        return median;
    }

}
