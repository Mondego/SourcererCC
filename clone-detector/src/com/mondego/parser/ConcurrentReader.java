package com.mondego.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ConcurrentReader {
    public long startId;
    public long endId;
    public String idFile;

    public ConcurrentReader() throws FileNotFoundException {
        this.startId = 0;
        this.endId = 0;
        this.idFile = "idgen.txt";
        this.updateIds();
    }

    public void updateIds() throws FileNotFoundException {
        File file = new File(this.idFile);
        FileLock lock = null;
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();
        try {
            lock = channel.lock();
            long fileSize = channel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            channel.read(buffer);
            String line = new String(buffer.array());
            this.startId = Long.parseLong(line.trim())+100;
            this.endId = this.startId+ 200;
            ByteBuffer outBuffer = ByteBuffer.allocate(8);
            outBuffer.clear();
            String endidStr= this.endId+"";
            outBuffer.put(endidStr.getBytes());
            outBuffer.flip();
            channel.write(outBuffer,0);
            channel.force(false);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                lock.release();
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void printIds() throws FileNotFoundException{
        while(true){
            System.out.println(this.startId);
            this.startId++;
            if(this.startId==this.endId){
                this.updateIds();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }

    public static void main(String[] args) throws FileNotFoundException {
        ConcurrentReader cr = new ConcurrentReader();
        cr.printIds();
    }
}
