package com.mondego.models;
import java.text.ParseException;

public interface ITokensFileProcessor {
    public void processLine(String line,boolean processCompleteLine) throws ParseException;
}
