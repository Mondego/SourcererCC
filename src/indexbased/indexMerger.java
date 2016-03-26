package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.Util;

public class indexMerger {
    private List<FSDirectory> invertedIndexDirectories;
    private List<FSDirectory> forwardIndexDirectories;

    public indexMerger() {
        super();
        this.invertedIndexDirectories = new ArrayList<FSDirectory>();
        this.forwardIndexDirectories = new ArrayList<FSDirectory>();
    }

    private void populateIndeXdirs(String inputFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    inputFile), "UTF-8"));
            String line;
            int count =0;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                System.out.println(count + ", "+line);
                count++;
                String invertedIndexDirPath = line+"/index";
                String forwardIndexDirPath = line+"/fwd/index";
                FSDirectory idir = FSDirectory.open(new File(invertedIndexDirPath));
                this.invertedIndexDirectories.add(idir);
                FSDirectory fdir = FSDirectory.open(new File(forwardIndexDirPath));
                this.forwardIndexDirectories.add(fdir);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void mergeindexes() {
        // TODO Auto-generated method stub
        System.out.println("mering inverted indexes");
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, whitespaceAnalyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);
        IndexWriter indexWriter = null;
        try {
            FSDirectory dir = FSDirectory.open(new File(Util.INDEX_DIR));
            indexWriter = new IndexWriter(dir, indexWriterConfig);
            FSDirectory[] dirs = this.invertedIndexDirectories
                    .toArray(new FSDirectory[this.invertedIndexDirectories
                            .size()]);
            indexWriter.addIndexes(dirs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("mergin fwd indexes");
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, keywordAnalyzer);
        fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
        try {

            FSDirectory dir = FSDirectory.open(new File(Util.FWD_INDEX_DIR));
            indexWriter = new IndexWriter(dir, fwdIndexWriterConfig);
            FSDirectory[] dirs = this.forwardIndexDirectories
                    .toArray(new FSDirectory[this.forwardIndexDirectories
                            .size()]);
            indexWriter.addIndexes(dirs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String [] args){
        indexMerger indexMerger = new indexMerger();
        String inputFile = "/home/sourcerer/hades/hpc_backup/kmaster";
        System.out.println("populating index dirs");
        indexMerger.populateIndeXdirs(inputFile);
        System.out.println("merging");
        indexMerger.mergeindexes();
        System.out.println("done!");
    }
}
