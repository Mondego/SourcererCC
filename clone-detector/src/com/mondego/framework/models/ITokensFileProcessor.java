package com.mondego.framework.models;
import java.text.ParseException;

public interface ITokensFileProcessor {
    public void processLine(String line) throws ParseException;
}
