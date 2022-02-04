package org.finos.waltz.common;

import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;

public class FunctionUtilities_getTimeUsingRunnableTest {

    @Test
    public void timeDurationOfRunnableWithEmptyName(){
        FunctionUtilities.time("", Unchecked.runnable(() -> { Thread.sleep(500); }));
    }

    @Test
    public void timeDurationOfRunnableWithNullName(){
        FunctionUtilities.time(null, Unchecked.runnable(() -> { Thread.sleep(500); }));
    }

    @Test
    public void timeDurationOfRunnable(){
        FunctionUtilities.time("foo", Unchecked.runnable(() -> { Thread.sleep(500); }));
    }

    @Test
    public void timeDurationOfRunnableZeroSec(){
        FunctionUtilities.time("foo", Unchecked.runnable(() -> { Thread.sleep(0); }));
    }

    @Test
    public void timeDurationOfRunnableNegSec() throws IllegalArgumentException{
        FunctionUtilities.time("foo", Unchecked.runnable(() -> { Thread.sleep(-1); }));
    }

}
