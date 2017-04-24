package preprocessing;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.interval.IntervalUtils;

/**
 * Created by Farima 
 */
public class Preprocessor {
    public static void main(String[] args) {
		long beginTime=System.currentTimeMillis();
        addStatsAndGiveIdsToTokens();
		long endTime=System.currentTimeMillis();
        System.out.println(endTime-beginTime);
    }
    private static void addStatsAndGiveIdsToTokens(){
                int randomNumber=0;
        try {
            String inputFileName=new File(Paths.get("./input_preprocessing").toString()).listFiles()[0].getName();
            String ouputFileName=inputFileName+"_preprocessed";

            String eachFile="";
            System.out.println(ouputFileName);
            BufferedReader bf=new BufferedReader(new FileReader(Paths.get("./input_preprocessing").toString()+"/"+inputFileName));
            PrintWriter writer=new PrintWriter((Paths.get("./output_preprocessing/").toString()+"/"+ouputFileName)+".txt");
            HashMap<String,Integer> wordNumbers=new HashMap<>();
            int charactersNum=0;
            while ((eachFile=bf.readLine())!=null) {
                String[] separatedLineFromMeta = eachFile.split("@#@");
                if (Integer.parseInt(separatedLineFromMeta[0].split(",")[2]) >=65&&Integer.parseInt(separatedLineFromMeta[0].split(",")[2]) <=500000) {//we do calculations just for files have more than 65 tokens and less than 5000 tokens
                    String[] words = null;
                    try {
                        words = separatedLineFromMeta[1].split(",");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    String word = "";
                    String lineNewFile = "";
                    double mean;
                    double variance;
                    double mad;
                    double stdDev;
                    double median;
                    double mode;
                    double skewness;
                    double kurtosis;
                    ArrayList<Double> numbersInLine = new ArrayList<>();
                    for (int i = 0; i < words.length; i++) {
                        word = words[i].split("@@::@@")[0];

                        Integer frequency = Integer.valueOf(words[i].split("@@::@@")[1]);
                        if (!wordNumbers.containsKey(word)) {
                            randomNumber++;
                            wordNumbers.put(word, randomNumber);
                        }
                        for (int j = 0; j < frequency; j++) {
                            numbersInLine.add(Double.valueOf(wordNumbers.get(word)));
                            charactersNum += word.length();
                        }

                        lineNewFile += words[i] + "@@::@@" + wordNumbers.get(word) + ",";
                    }
                    double[] numbers = new double[numbersInLine.size()];
                    for (int i = 0; i < numbers.length; i++) {
                        numbers[i] = numbersInLine.get(i);
                    }
                    Skewness sk=new Skewness();
                    Kurtosis ku=new Kurtosis();
                    skewness=sk.evaluate(numbers,0,numbers.length);
                    kurtosis=ku.evaluate(numbers,0,numbers.length);
                    median = getMedian(numbers);
                    variance = StatUtils.variance(numbers);
                    stdDev = Math.sqrt(variance);
                    mean = StatUtils.mean(numbers);
                    mad = getMad(numbers);
                    List<Map.Entry<Double, Integer>> sortedModes =
                            (getMode(numbers)).entrySet().stream().sorted(Map.Entry.<Double, Integer>comparingByValue().reversed()).limit(5).collect(Collectors.toList());
//                if (separatedLineFromMeta[0].startsWith("19,915"))
//                    System.out.println("d");
                    writer.append(separatedLineFromMeta[0] + "," + charactersNum + "," + median + "," + mean + "," + variance + "," + stdDev
                            + "," + mad + ","+skewness+","+kurtosis+","
                            + (sortedModes.size() > 0 ? sortedModes.get(0).getKey() + "#" + sortedModes.get(0).getValue() : "-1#-1") + ","
                            + (sortedModes.size() > 1 ? sortedModes.get(1).getKey() + "#" + sortedModes.get(1).getValue() : "-1#-1") + ","
                            + (sortedModes.size() > 2 ? sortedModes.get(2).getKey() + "#" + sortedModes.get(2).getValue() : "-1#-1") + ","
                            + (sortedModes.size() > 3 ? sortedModes.get(3).getKey() + "#" + sortedModes.get(3).getValue() : "-1#-1") + ","
                            + (sortedModes.size() > 4 ? sortedModes.get(4).getKey() + "#" + sortedModes.get(4).getValue() : "-1#-1") + "@#@"
                            + lineNewFile.substring(0, lineNewFile.length() - 1) + System.lineSeparator());
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


    private static double getMedian(double[] numArray) {
        Arrays.sort(numArray);
        double median;
        if (numArray.length % 2 == 0)
            median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
        else
            median = (double) numArray[numArray.length/2];
        return median;
    }
	
	private static double getMad(double[] numArray){
        double mean=StatUtils.mean(numArray);
        double[] medDiffenences=new double[numArray.length];
        for (int i = 0; i <numArray.length ; i++) {
            medDiffenences[i]=Math.abs(numArray[i]-mean);
        }
        return StatUtils.mean(medDiffenences);
    }
	
	private static HashMap<Double,Integer> getMode(double[] numarray){
        HashMap<Double,Integer> frequencies=new HashMap<>();
        HashMap<Double,Integer> modes=new HashMap<>();
        for (int i = 0; i <numarray.length ; i++) {
            if (frequencies.containsKey(numarray[i])){
                frequencies.put(numarray[i],frequencies.get(numarray[i])+1);
            }
            else {
                frequencies.put(numarray[i],1);
            }
        }

        Integer[] maxFreqs=new Integer[frequencies.keySet().size()];
        int i=0;
        for (Double number:frequencies.keySet()){
            maxFreqs[i]=frequencies.get(number);
            i++;
        }
        Arrays.sort(maxFreqs, Collections.reverseOrder()) ;
        for (Double number:frequencies.keySet()){
            if (frequencies.get(number)==maxFreqs[0]) modes.put(number,maxFreqs[0]);
            else if (frequencies.get(number)==maxFreqs[1]) modes.put(number,maxFreqs[1]);
            else if (frequencies.get(number)==maxFreqs[2]) modes.put(number,maxFreqs[2]);
            else if (frequencies.get(number)==maxFreqs[3]) modes.put(number,maxFreqs[3]);
            else if (frequencies.get(number)==maxFreqs[4]) modes.put(number,maxFreqs[4]);
        }
        return modes;
    }

}
