package com.mondego.application.workers;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.mondego.application.models.QueryBlock;
import com.mondego.application.models.TokenInfo;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.services.RecoveryService;
import com.mondego.framework.workers.Worker;
import com.mondego.utility.TokensFileReader;

public class QueryLineProcessorWorker extends Worker<String> {
    private RecoveryService recoveryService;
    public QueryLineProcessorWorker(String t) {
        super(t);
        this.recoveryService = RecoveryService.getInstance();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void process() {
        // TODO Auto-generated method stub
        long startTime = System.nanoTime();
        try {
            QueryBlock queryBlock = this.getNextQueryBlock(this.dataObject);
            if (queryBlock == null)
                return;
            if (MainController.appendToExistingFile && this.recoveryService.completedQueries.contains(queryBlock.getId())) {
                logger.debug("ignoring query, REASON: completed in previous run, " + queryBlock);
                return;
            }

            if (MainController.isStatusCounterOn) {
                MainController.statusCounter += 1;
            }
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(MainController.NODE_PREFIX + " QLP processed QueryBlock " + queryBlock + " in "
                    + estimatedTime / 1000 + " micros");
            this.pipe.getChannel("FIND_CANDIDATES").send(queryBlock);
            // System.out.println(SearchManager.NODE_PREFIX +
            // ", line number: "+ count);
        } catch (InstantiationException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage() + " skiping this query block, illegal args: " + this.dataObject.substring(0, 40));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error("catching parseException, dont worry");
            logger.error(e.getMessage() + " skiping this query block, parse exception: " + this.dataObject.substring(0, 40));
            // e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    public QueryBlock getNextQueryBlock(String line) throws ParseException, IllegalArgumentException {
        List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
        QueryBlock queryBlock = TokensFileReader.getSortedQueryBlock(line, listOfTokens);
        if (queryBlock == null) {
            logger.debug(MainController.NODE_PREFIX + " QLP, Invalid QueryBlock " + line.substring(0, 40));
            return null;
        }

        int position = 0;
        for (Entry<String, TokenInfo> entry : listOfTokens) {
            TokenInfo tokenInfo = entry.getValue();
            if (position < queryBlock.getPrefixSize()) {
                queryBlock.getPrefixMap().put(entry.getKey(), tokenInfo);
                position += tokenInfo.getFrequency();
                queryBlock.setPrefixMapSize(position);
            } else {
                queryBlock.getSuffixMap().put(entry.getKey(), tokenInfo);
                position += tokenInfo.getFrequency();
            }
            tokenInfo.setPosition(position);
        }

        return queryBlock;
    }

}
