package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DebugUtilities_logValueFromResultTest {
    @Test
    public void logEmptyMessage(){
       String result = DebugUtilities.logValue("", "aaa");
       assertEquals("", result);
    }

    @Test
    public void logNullResult(){
        String result = DebugUtilities.logValue("Logged values from result", null);
        assertEquals("Logged values from result", result);
    }

    @Test
    public void logEmptyResult(){
        String result = DebugUtilities.logValue("Logged values from result", "");
        assertEquals("Logged values from result", result);
    }



    @Test
    public void logEmptyResultAndMessage(){
        String result = DebugUtilities.logValue("", "");
        assertEquals("", result);
    }

    @Test
    public void logResultAndMessage(){
        String result = DebugUtilities.logValue("Logged values from result", "aaa");
        assertEquals("Logged values from result", result);
    }
}
