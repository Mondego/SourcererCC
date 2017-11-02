package com.mondego.framework.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.application.config.ApplicationProperties;
import com.mondego.framework.config.FrameworkProperties;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.utility.Util;

public class RuntimeStateService {
    private static RuntimeStateService instance;
    private static final Logger logger = LogManager
            .getLogger(RuntimeStateService.class);
    public long timeSearch;
    private long RUN_COUNT;
    private int numCandidates;
    public int clonePairsCount;
    private int docId;

    private RuntimeStateService() {
    }

    public static synchronized RuntimeStateService getInstance() {
        if (null == instance) {
            instance = new RuntimeStateService();
        }
        return instance;
    }

    public void signOffNode() {
        logger.debug("signing off " + MainController.NODE_PREFIX);
        File file = new File(MainController.completedNodes);
        FileLock lock = null;
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "rwd");
            FileChannel channel = raf.getChannel();
            try {
                lock = channel.lock();
                logger.debug("lock obtained? " + lock);
                ByteBuffer outBuffer = ByteBuffer.allocate(100);
                outBuffer.clear();
                String endidStr = MainController.NODE_PREFIX + "\n";
                outBuffer.put(endidStr.getBytes());
                outBuffer.flip();
                // System.out.println(new String(outBuffer.array()));
                channel.write(outBuffer, raf.length());
                channel.force(false);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            } finally {
                try {
                    lock.release();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            logger.error(e1.getMessage());
        }

    }

    public int getCompletedNodes() {
        File completedNodeFile = new File(MainController.completedNodes);
        FileLock lock = null;
        int count = 0;
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(completedNodeFile, "rw");
            FileChannel channel = raf.getChannel();
            try {
                lock = channel.lock();
                while (raf.readLine() != null) {
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    lock.release();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return count;
    }

    public int getNodes() {
        if (-1 == MainController.totalNodes) {
            File searchMertadaFile = new File(FrameworkProperties.SEARCH_METADATA);
            try {
                BufferedReader br = Util.getReader(searchMertadaFile);
                String line = br.readLine();
                if (null != line) {
                    MainController.totalNodes = Integer.parseInt(line.trim());
                    return MainController.totalNodes;
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return MainController.totalNodes;
    }

    public boolean allNodesCompleted() {
        return 0 == (this.getNodes() - this.getCompletedNodes());
    }

    public void backupInput() {
        String previousDataFolder = MainController.DATASET_DIR + "/oldData/";
        Util.createDirs(previousDataFolder);
        File sourceDataFile = new File(
                MainController.DATASET_DIR + "/" + ApplicationProperties.QUERY_FILE_NAME);
        String targetFileName = previousDataFolder + System.currentTimeMillis()
                + "_" + ApplicationProperties.QUERY_FILE_NAME;
        sourceDataFile.renameTo(new File(targetFileName));
        File completedNodesFile = new File(MainController.completedNodes);
        completedNodesFile.delete();// delete the completedNodes file
    }

    public void readAndUpdateRunMetadata() {

        this.readRunMetadata();
        // update the runMetadata
        this.RUN_COUNT += 1;
        this.updateRunMetadata(this.RUN_COUNT + "");
    }

    private void readRunMetadata() {
        File f = new File(FrameworkProperties.RUN_METADATA);
        BufferedReader br = null;
        if (f.exists()) {
            logger.debug(FrameworkProperties.RUN_METADATA
                    + " file exists, reading it to get the run metadata");
            try {
                br = Util.getReader(f);
                String line = br.readLine().trim();
                if (!line.isEmpty()) {
                    this.RUN_COUNT = Long.parseLong(line);
                    logger.debug("last run count was: " + this.RUN_COUNT);
                } else {
                    this.RUN_COUNT = 1;
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.RUN_COUNT = 1;
        }

    }

    private void updateRunMetadata(String text) {
        File f = new File(FrameworkProperties.RUN_METADATA);
        try {
            Writer writer = Util.openFile(f, false);
            Util.writeToFile(writer, text, true);
            Util.closeFile(writer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void backupOutput() throws IOException {
        this.readRunMetadata();
        String destDir = ApplicationProperties.OUTPUT_BACKUP_DIR + "/" + this.RUN_COUNT + "/"
                + MainController.NODE_PREFIX;
        Util.createDirs(destDir); // creates if it doesn't exist
        String sourceDir = MainController.OUTPUT_DIR
                + MainController.th / MainController.MUL_FACTOR;
        logger.debug("moving " + sourceDir + " to " + destDir);
        // Copy the output folder instead of moving it.
        FileUtils.copyDirectory(new File(sourceDir), new File(destDir), true);
    }

    public synchronized void updateNumCandidates(int num) {
        this.numCandidates += num;
    }

    public synchronized void updateClonePairsCount(int num) {
        this.clonePairsCount += num;
    }

    public synchronized long getNextId() {
        // TODO Auto-generated method stub
        this.docId++;
        return this.docId;
    }

}
