package models;

import java.lang.reflect.InvocationTargetException;
import indexbased.SearchManager;

public class QueryFileProcessor implements ITokensFileProcessor {

    public QueryFileProcessor() {
    }
    
    @Override
    public void processLine(String line) {
	try {
	    SearchManager.queryLineQueue.send(line);
	} catch (InstantiationException e){
            e.printStackTrace();
	} catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()
			       + " skiping this query block, illegal args: " + line.substring(0,40));
            e.printStackTrace();
	} catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
	}
    }

}
