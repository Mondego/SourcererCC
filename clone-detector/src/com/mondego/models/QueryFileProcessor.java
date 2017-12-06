package com.mondego.models;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class QueryFileProcessor implements ITokensFileProcessor {
    private static final Logger logger = LogManager.getLogger(QueryFileProcessor.class);
    Shard shard;
    public QueryFileProcessor(Shard shard) {
        this.shard= shard;
    }

    @Override
    public void processLine(String line) {
        try {
            
            SearchManager.queryLineQueue.send(new QueryLineWrapper(line, this.shard));
        } catch (InstantiationException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage()
                    + " skiping this query block, illegal args: "
                    + line.substring(0, 40));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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
        }
    }

}
