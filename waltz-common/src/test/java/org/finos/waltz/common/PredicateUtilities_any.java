package org.finos.waltz.common;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PredicateUtilities_any {
    @Test
    public void simpleAnyTrueCase1(){
        List<Integer> ele = ListUtilities.newArrayList(2,4);
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test
    public void simpleAnyFalseCase1(){
        List<Integer> ele = ListUtilities.newArrayList(1,3);
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAny1(){
        List<Integer> ele = ListUtilities.newArrayList(1,2,3);
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullList1(){
        List<Integer> ele = null;
        PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullPredicate1(){
        List<Integer> ele = ListUtilities.newArrayList(1,2,3);;
        PredicateUtilities.any(ele, null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyWithAllNull1(){
        List<Integer> ele = null;
        PredicateUtilities.any(ele, null);
    }

    @Test
    public void simpleAnyTrueCase2(){
        Integer[] ele = new Integer[]{2,4};
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test
    public void simpleAnyFalseCase2(){
        Integer[] ele = new Integer[]{1,3};
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAny2(){
        Integer[] ele = new Integer[]{1,2,3};
        boolean p = PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullList2(){
        Integer[] ele = null;
        PredicateUtilities.any(ele, x->(Integer.parseInt(x.toString())%2)==0);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullPredicate2(){
        Integer[] ele = new Integer[]{1,2,3};
        PredicateUtilities.any(ele, null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyWithAllNull2(){
        Integer[] ele = null;
        PredicateUtilities.any(ele, null);
    }
}
