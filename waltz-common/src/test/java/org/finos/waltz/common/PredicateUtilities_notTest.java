package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class PredicateUtilities_notTest {
    @Test
    public void simpleNotTrueCase(){
        Predicate p = PredicateUtilities.not(x->(Integer.parseInt(x.toString())%2)==0);
        assertTrue(p.test(1));
    }

    @Test
    public void simpleNotFalseCase(){
        Predicate p = PredicateUtilities.not(x->(Integer.parseInt(x.toString())%2)==0);
        assertFalse(p.test(2));
    }

    @Test
    public void simpleNotNullPredicate() {
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.not(null));
    }
}
