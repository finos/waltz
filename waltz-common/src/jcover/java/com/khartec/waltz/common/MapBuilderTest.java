package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.HashMap;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.MapBuilder
 *
 * @author Diffblue JCover
 */

public class MapBuilderTest {

    @Test
    public void addKIsFooAndVIsFoo() {
        MapBuilder<String, String> mapBuilder = new MapBuilder<String, String>();
        assertThat(mapBuilder.add("foo", "foo"), sameInstance(mapBuilder));
    }

    @Test
    public void buildReturnsEmpty() {
        assertThat(new MapBuilder<String, String>().build().isEmpty(), is(true));
    }

    @Test
    public void fromMIsEmpty() {
        MapBuilder<String, String> mapBuilder = new MapBuilder<String, String>();
        assertThat(mapBuilder.from(new HashMap<String, String>()), sameInstance(mapBuilder));
    }
}
