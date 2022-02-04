package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PredicateUtilities_noneTest {
    @Test
    public void simpleNoneFalseCase1(){
        List<Integer> ele = ListUtilities.newArrayList(2,4);
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertFalse(p);
    }

    @Test
    public void simpleNoneTrueCase1(){
        List<Integer> ele = ListUtilities.newArrayList(1,3);
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertTrue(p);
    }

    @Test
    public void simpleNone1(){
        List<Integer> ele = ListUtilities.newArrayList(1,2,3);
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertFalse(p);
    }

    @Test
    public void simpleNoneNullList1() {
        List<Integer> ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, x -> (Integer.parseInt(x.toString()) % 2) == 0));
    }

    @Test
    public void simpleNoneNullPredicate1() {
        List<Integer> ele = ListUtilities.newArrayList(1, 2, 3);
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, null));
    }

    @Test
    public void simpleNoneWithAllNull1() {
        List<Integer> ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, null));
    }

    @Test
    public void simpleNoneFalseCase2(){
        Integer[] ele = new Integer[]{2,4};
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleNoneTrueCase2(){
        Integer[] ele = new Integer[]{1,3};
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test
    public void simpleNone2(){
        Integer[] ele = new Integer[]{1,2,3};
        boolean p = PredicateUtilities.none(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleNoneNullList2() {
        Integer[] ele = null;

        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, x -> (Integer.parseInt(x.toString()) % 2) == 0));
    }

    @Test
    public void simpleNoneNullPredicate2() {
        Integer[] ele = new Integer[]{1, 2, 3};

        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, null));
    }

    @Test
    public void simpleNoneWithAllNull2() {
        Integer[] ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.none(ele, null));
    }
}
