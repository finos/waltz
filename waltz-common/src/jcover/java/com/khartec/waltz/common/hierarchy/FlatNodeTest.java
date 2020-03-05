package com.khartec.waltz.common.hierarchy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.hierarchy.FlatNode
 *
 * @author Diffblue JCover
 */

public class FlatNodeTest {

    @Test
    public void constructor() {
        Optional<String> parentId = Optional.<String>empty();
        FlatNode<String, String> flatNode = new FlatNode<String, String>("root", parentId, "something");
        assertThat(flatNode.getData(), is("something"));
        assertThat(flatNode.getId(), is("root"));
        assertThat(flatNode.getParentId(), sameInstance(parentId));
    }

    @Test
    public void getParentId() {
        Optional<String> parentId = Optional.<String>empty();
        assertThat(new FlatNode<String, String>("root", parentId, "something").getParentId(), sameInstance(parentId));
    }

    @Test
    public void getters() {
        assertThat(new FlatNode<String, String>("root", Optional.<String>empty(), "something").getData(), is("something"));
        assertThat(new FlatNode<String, String>("root", Optional.<String>empty(), "something").getId(), is("root"));
    }
}
