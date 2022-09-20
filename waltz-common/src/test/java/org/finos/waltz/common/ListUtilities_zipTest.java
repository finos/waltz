package org.finos.waltz.common;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_zipTest {

    @Test
    public void zippedListOrderedByIndexOfOriginal() {
        List<String> xs = ListUtilities.newArrayList("a", "b", "c");
        List<Integer> ys = ListUtilities.newArrayList(1, 2, 3);
        List<Tuple2<String, Integer>> zippedList = ListUtilities.zip(xs, ys);
        assertEquals(
                asList(tuple("a", 1), tuple("b", 2), tuple("c", 3)),
                zippedList,
                "Zipped lists are ordered correctly");
    }

    @Test
    public void zippedListLimitedToShortestInputCollectionXs() {
        List<String> xs = ListUtilities.newArrayList("a", "b");
        List<Integer> ys = ListUtilities.newArrayList(1, 2, 3);
        List<Tuple2<String, Integer>> zippedList = ListUtilities.zip(xs, ys);
        assertEquals(2, zippedList.size(), "Zipped list should only be as long as the shortest collection provided");
        assertEquals(
                asList(tuple("a", 1), tuple("b", 2)),
                zippedList,
                "Zipped list should only be as long as the shortest collection provided");
    }

    @Test
    public void zippedListLimitedToShortestInputCollectionYs() {
        List<String> xs = ListUtilities.newArrayList("a", "b", "c");
        List<Integer> ys = ListUtilities.newArrayList(1, 2);
        List<Tuple2<String, Integer>> zippedList = ListUtilities.zip(xs, ys);
        assertEquals(2, zippedList.size(), "Zipped list should only be as long as the shortest collection provided");
        assertEquals(
                asList(tuple("a", 1), tuple("b", 2)),
                zippedList,
                "Zipped list should only be as long as the shortest collection provided");
    }

    @Test
    public void zippedListCanHandleNulls() {
        List<String> xs = ListUtilities.newArrayList("a", null, "c");
        List<Integer> ys = ListUtilities.newArrayList(1, 2, 3);
        List<Tuple2<String, Integer>> zippedList = ListUtilities.zip(xs, ys);
        assertEquals(
                asList(tuple("a", 1), tuple(null, 2), tuple("c", 3)),
                zippedList,
                "Zipped list should zip nulls");
    }

}
