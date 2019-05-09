package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Optional;

import static com.khartec.waltz.common.OptionalUtilities.ofNullableOptional;
import static org.junit.Assert.*;

public class OptionalUtilities_ofNullableOptional {

    @Test
    public void givingNullReturnsEmpty() {
        assertEquals(
                Optional.empty(),
                ofNullableOptional(null));
    }


    @Test
    public void givingEmptyReturnsEmpty() {
        assertEquals(
                Optional.empty(),
                ofNullableOptional(Optional.empty()));
    }


    @Test
    public void givingSomethingReturnsTheSameSomething() {
        assertEquals(
                Optional.of("hello"),
                ofNullableOptional(Optional.of("hello")));
    }

}