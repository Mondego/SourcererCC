package com.mondego.models;
import java.text.ParseException;

public interface ITokensFileProcessor {
    public void processLine(String line) throws ParseException;
}
