package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class CandidateProcessor implements IListener, Runnable {
    private QueryCandidates qc;
    private static final Logger logger = LogManager.getLogger(CandidateProcessor.class);

    public CandidateProcessor(QueryCandidates qc) {
        // TODO Auto-generated constructor stub
        this.qc = qc;
    }

    @Override
    public void run() {
        try {
            // System.out.println( "QCQ size: "+
            // SearchManager.queryCandidatesQueue.size() + Util.debug_thread());
            this.processCandidates();
        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    private void processCandidates() throws InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    		//CandidatePair candidatePair = new CandidatePair(queryBlock, simInfo, computedThreshold, candidateSize, functionIdCandidate, candidateId)
    	logger.debug("num candidates before: "+ (this.qc.candidateUpperIndex-this.qc.candidateLowerIndex));
    	if (SearchManager.ijaMapping.containsKey(this.qc.queryBlock.fqmn)){
    		logger.debug("num candidates: "+ (this.qc.candidateUpperIndex-this.qc.candidateLowerIndex));
    		for (int i=this.qc.candidateLowerIndex;i<=this.qc.candidateUpperIndex;i++){
    			Block candidateBlock = SearchManager.candidatesList.get(i);
    			if(candidateBlock.rowId < this.qc.queryBlock.rowId){
    				if(SearchManager.ijaMapping.containsKey(candidateBlock.fqmn)){ // check if candidate's fqmn is in the map
    					this.writeToSocket(this.getLineToWrite(qc.queryBlock, candidateBlock));
    				}
    			}
    		}
    		
    	//SearchManager.verifyCandidateQueue.send(candidatePair);
        }
    }
    private String[] getLineToWrite(Block queryBlock, Block candiadteBlock){
        String output[]=new String[30];
//        output[0]=firstLine[0]+"."+firstLine[1]+"."+firstLine[2]+"."+firstLine[27];
//        output[1]=secondLine[0]+secondLine[1]+secondLine[2]+secondLine[27];
       // output[2]=isClone?"1":"0";
        output[0]=queryBlock.projectName+","+queryBlock.fileName+","+queryBlock.startLine+","+queryBlock.endLine;
        output[1]=candiadteBlock.projectName+","+candiadteBlock.fileName+","+candiadteBlock.startLine+","+candiadteBlock.endLine;
        
        String cp = queryBlock.functionId+","+queryBlock.id+","+candiadteBlock.functionId+","+candiadteBlock.id;
        if (SearchManager.clonePairs.contains(cp)){
        	output[2]=1+"";
        }else{
        	cp = candiadteBlock.functionId+","+candiadteBlock.id+","+queryBlock.functionId+","+queryBlock.id;
        	if(SearchManager.clonePairs.contains(cp)){
        		output[2]=1+"";
        	}else{
        		output[2]=0+"";
        	}
        }
        
        for (int i = 3; i <output.length ; i++) {
        	
            output[i]=roundTwoDecimal(getPercentageDiff(queryBlock.metrics.get(i-3),candiadteBlock.metrics.get(i-3))).toString();
        }
        return output;
    }
    private Double getPercentageDiff(double firstValue,double secondValue){
        return (Math.abs(firstValue-secondValue)/Math.max(firstValue,secondValue))*100;
    }

    private Double roundTwoDecimal(double param){
        return Double.valueOf(Math.round(param*100.0)/100.0);
    }
    private void writeToSocket(String[] lineParams){
        String line="";
        for (int i = 0; i <lineParams.length ; i++) {
            line+=lineParams[i]+"~~";
        }
        line=line.substring(0,line.length()-2);//changed to 2 becasue ~~ has to chars
        try{
        	
            SearchManager.socketWriter.writeToSocket(line);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
