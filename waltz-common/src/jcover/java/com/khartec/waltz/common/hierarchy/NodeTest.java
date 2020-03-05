package com.khartec.waltz.common.hierarchy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertTrue;

import com.khartec.waltz.common.Aliases;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.hierarchy.Node
 *
 * @author Diffblue JCover
 */

public class NodeTest {

    @Test
    public void addChild() {
        Node<String, String> node = new Node<String, String>("root", "something");
        assertThat(node.addChild(new Node(new Aliases(), new Aliases())), sameInstance(node));
        assertThat(node.getChildren().size(), is(1));
    }

    @Test
    public void getters() {
        assertTrue(new Node<String, String>("root", "something").getChildren().isEmpty());
        assertThat(new Node<String, String>("root", "something").getData(), is("something"));
        assertThat(new Node<String, String>("root", "something").getId(), is("root"));
        assertThat(new Node<String, String>("root", "something").getParent(), is(nullValue()));
    }

    @Test
    public void setParent() {
        Node<String, String> node = new Node<String, String>("root", "something");
        Node<String, String> parent = new Node<String, String>("root", "something");
        assertThat(node.setParent(parent), sameInstance(node));
        assertThat(node.getParent(), sameInstance(parent));
    }
}
