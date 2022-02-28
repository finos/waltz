package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PredicateUtilities_allTest {
    @Test
    public void simpleAllTrueCase1(){
        List<Integer> ele = ListUtilities.newArrayList(2,4);
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test
    public void simpleAllFalseCase1(){
        List<Integer> ele = ListUtilities.newArrayList(1,3);
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAll1(){
        List<Integer> ele = ListUtilities.newArrayList(1,2,3);
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAllNullList1() {
        List<Integer> ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, x -> (Integer.parseInt(x.toString()) % 2) == 0));
    }

    @Test
    public void simpleAllNullPredicate1() {
        List<Integer> ele = ListUtilities.newArrayList(1, 2, 3);
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, null));
    }

    @Test
    public void simpleAllNull1() {
        List<Integer> ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, null));
    }

    @Test
    public void simpleAllTrueCase2(){
        Integer[] ele = new Integer[]{2,4};
        assertTrue(PredicateUtilities.all(ele, x -> (Integer.parseInt(x.toString()) % 2) == 0));
    }

    @Test
    public void simpleAllFalseCase2(){
        Integer[] ele = new Integer[]{1,3};
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertFalse(p);
    }

    @Test
    public void simpleAll2(){
        Integer[] ele = new Integer[]{1,2,3};
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAllNullList2() {
        Integer[] ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, x -> (Integer.parseInt(x.toString()) % 2) == 0));
    }

    @Test
    public void simpleAllNullPredicate2() {
        Integer[] ele = new Integer[]{1, 2, 3};
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, null));

    }

    @Test
    public void simpleAllNull2() {
        Integer[] ele = null;
        assertThrows(NullPointerException.class,
                () -> PredicateUtilities.all(ele, null));
    }
}
