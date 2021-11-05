package org.finos.waltz.common;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PredicateUtilities_all {
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

    @Test(expected = NullPointerException.class)
    public void simpleAllNullList1(){
        List<Integer> ele = null;
        PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAllNullPredicate1(){
        List<Integer> ele = ListUtilities.newArrayList(1,2,3);;
        PredicateUtilities.all(ele, null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAllNull1(){
        List<Integer> ele = null;
        PredicateUtilities.all(ele, null);
    }

    @Test
    public void simpleAllTrueCase2(){
        Integer[] ele = new Integer[]{2,4};
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(true,p);
    }

    @Test
    public void simpleAllFalseCase2(){
        Integer[] ele = new Integer[]{1,3};
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test
    public void simpleAll2(){
        Integer[] ele = new Integer[]{1,2,3};
        boolean p = PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
        assertEquals(false,p);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAllNullList2(){
        Integer[] ele = null;
        PredicateUtilities.all(ele, x->(Integer.parseInt(x.toString())%2)==0);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAllNullPredicate2(){
        Integer[] ele = new Integer[]{1,2,3};
        PredicateUtilities.all(ele, null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAllNull2(){
        Integer[] ele = null;
        PredicateUtilities.all(ele, null);
    }
}
