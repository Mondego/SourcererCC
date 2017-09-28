package com.mondego.framework.models;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.application.handlers.SearchActionHandler;
import com.mondego.framework.pipeline.Pipe;

public class QueryFileProcessor implements ITokensFileProcessor {
    private Pipe pipe;
    private static final Logger logger = LogManager.getLogger(QueryFileProcessor.class);
    public QueryFileProcessor() {
        this.pipe = Pipe.getInstance();
    }

    @Override
    public void processLine(String line) {
        try {
            this.pipe.getChannel("READ_LINE").send(line);
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
