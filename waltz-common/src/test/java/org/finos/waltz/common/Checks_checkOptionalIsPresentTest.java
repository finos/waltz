package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Checks_checkOptionalIsPresentTest {

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

    @Test
    public void sendNull() {
        Optional element = Optional.ofNullable(null);
        assertThrows(IllegalArgumentException.class,
                () -> Checks.checkOptionalIsPresent(element, "Test"));

    }

    @Test
    public void sendNothing() {
        Optional element = Optional.empty();
        assertThrows(IllegalArgumentException.class,
                () -> Checks.checkOptionalIsPresent(element, "Test"));
    }
}
