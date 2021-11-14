package org.finos.waltz.common;

import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class PredicateUtilities_notTest {
    @Test
    public void simpleNotTrueCase(){
        Predicate p = PredicateUtilities.not(x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p.test(1));
    }

    @Test
    public void simpleNotFalseCase(){
        Predicate p = PredicateUtilities.not(x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p.test(2));
    }

    @Test(expected = NullPointerException.class)
    public void simpleNotNullPredicate(){
        PredicateUtilities.not(null);
    }
}
