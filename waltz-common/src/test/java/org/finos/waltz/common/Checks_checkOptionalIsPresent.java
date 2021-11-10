package org.finos.waltz.common;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class Checks_checkOptionalIsPresent {

    @Test
    public void sendElement(){
        Optional element = Optional.ofNullable(1);
        Object result = Checks.checkOptionalIsPresent(element,"Test");
        assertEquals(1,result);
    }

    @Test
    public void sendEmptyStringElement(){
        Optional element = Optional.ofNullable("");
        Object result = Checks.checkOptionalIsPresent(element,"Test");
        assertEquals("",result);
    }

    @Test(expected=IllegalArgumentException.class)
    public void sendNull(){
        Optional element = Optional.ofNullable(null);
        Object result = Checks.checkOptionalIsPresent(element,"Test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void sendNothing(){
        Optional element = Optional.empty();
        Object result = Checks.checkOptionalIsPresent(element,"Test");
    }
}
