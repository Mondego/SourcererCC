package uci.mondego;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Util {
    public static Writer openFile(String filename, boolean append)
            throws IOException {
        try {
            Writer pWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename, append), "UTF-8"),1024*1000*10);
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
