package com.mondego.models;

public class CloneLabel {
    int parentIdOne;
    int parentIdTwo;
    long blockIdOne;
    long blockIdTwo;

    public CloneLabel(String rawLine){
        String[] parts = rawLine.split(",");
        if (parts.length==4){
            this.parentIdOne = Integer.parseInt(parts[0]);
            this.blockIdOne = Long.parseLong(parts[1]);
            this.parentIdTwo = Integer.parseInt(parts[2]);
            this.blockIdTwo = Long.parseLong(parts[3]);
        }
    }
    
    
    public CloneLabel(int parentIdOne, long blockIdOne, int parentIdTwo, long blockIdTwo) {
        super();
        this.parentIdOne = parentIdOne;
        this.parentIdTwo = parentIdTwo;
        this.blockIdOne = blockIdOne;
        this.blockIdTwo = blockIdTwo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (blockIdOne ^ (blockIdOne >>> 32));
        result = prime * result + (int) (blockIdTwo ^ (blockIdTwo >>> 32));
        result = prime * result + parentIdOne;
        result = prime * result + parentIdTwo;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CloneLabel other = (CloneLabel) obj;
        if (blockIdOne != other.blockIdOne)
            return false;
        if (blockIdTwo != other.blockIdTwo)
            return false;
        if (parentIdOne != other.parentIdOne)
            return false;
        if (parentIdTwo != other.parentIdTwo)
            return false;
        return true;
    }

    
}
