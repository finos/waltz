package com.khartec.waltz.common.hierarchy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.hierarchy.HierarchyUtilities
 *
 * @author Diffblue JCover
 */

public class HierarchyUtilitiesTest {

    @Test
    public void assignDepthsReturnsEmpty() {
        assertThat(HierarchyUtilities.<String, String>assignDepths(new Forest<String, String>(new HashMap<String, Node<String, String>>(), new HashSet<Node<String, String>>())).isEmpty(), is(true));
    }

    @Test
    public void hasCycle1() {
        assertThat(HierarchyUtilities.<String, String>hasCycle(new Forest<String, String>(new HashMap<String, Node<String, String>>(), new HashSet<Node<String, String>>())), is(false));
    }

    @Test
    public void hasCycle2() {
        Map<String, Node<String, String>> allNodes = new HashMap<String, Node<String, String>>();
        ((HashMap<String, Node<String, String>>)allNodes).put("foo", new Node<String, String>("root", "something"));
        assertThat(HierarchyUtilities.<String, String>hasCycle(new Forest<String, String>(allNodes, new HashSet<Node<String, String>>())), is(false));
    }

    @Test
    public void parentsReturnsEmpty() {
        assertTrue(HierarchyUtilities.<String, String>parents(new Node<String, String>("root", "something")).isEmpty());
    }

    @Test
    public void toForestFlatNodesIsEmpty() {
        Forest<String, String> result = HierarchyUtilities.<String, String>toForest(new LinkedList<FlatNode<String, String>>());
        assertThat(result.getAllNodes().isEmpty(), is(true));
        assertTrue(result.getRootNodes().isEmpty());
    }
}
