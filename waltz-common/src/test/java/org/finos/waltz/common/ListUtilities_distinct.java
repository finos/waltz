package org.finos.waltz.common;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_distinct {

    @Test
    public void distinctDedupesListsButPreservesInitialOrder() {
        List<String> xs = asList("a", "b", "c", "c", "b", "c", "d");
        List<String> uniq = ListUtilities.distinct(xs);
        assertEquals(asList("a", "b", "c", "d"), uniq);
    }


}
