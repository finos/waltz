package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilities_splitThenMapTest {

    @Test
    public void simpleSplitThenMap(){
        String str = "a,b,c";
        List expectedList = ListUtilities.newArrayList("a","b","c");
        assertEquals(expectedList, StringUtilities.splitThenMap(str,",",x->x));
    }

    @Test
    public void simpleSplitThenMapWithNullTrans() {
        String str = "a,b,c";
        assertThrows(IllegalArgumentException.class,
                () -> StringUtilities.splitThenMap(str, ",", null));
    }

    @Test
    public void simpleSplitThenMapWithEmptySep(){
        String str = "a,b,c";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,"",x->x));
    }

    @Test
    public void simpleSplitThenMapWithNullSep(){
        String str = "a,b,c";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,null,x->x));
    }

    @Test
    public void simpleSplitThenMapWithEmptyStr(){
        String str = "";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,",",x->x));
    }

    @Test
    public void simpleSplitThenMapWithNullStr(){
        String str = null;
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,",",x->x));
    }

    @Test
    public void simpleSplitThenMapWith1(){
        String str = null;
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,"",x->x));
    }

    @Test
    public void simpleSplitThenMapWith2(){
        String str = "";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,null,x->x));
    }

    @Test
    public void simpleSplitThenMapWith3(){
        String str = "";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,"",x->x));
    }

    @Test
    public void simpleSplitThenMapWith4(){
        String str = null;
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.splitThenMap(str,null,x->x));
    }


}
