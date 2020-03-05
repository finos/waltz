package com.khartec.waltz.common.hierarchy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.hierarchy.Forest
 *
 * @author Diffblue JCover
 */

public class ForestTest {

    @Test
    public void getAllNodes() {
        Map<String, Node<String, String>> allNodes = new HashMap<String, Node<String, String>>();
        assertThat(new Forest<String, String>(allNodes, new HashSet<Node<String, String>>()).getAllNodes(), sameInstance(allNodes));
    }

    @Test
    public void getRootNodes() {
        Set<Node<String, String>> rootNodes = new HashSet<Node<String, String>>();
        assertThat(new Forest<String, String>(new HashMap<String, Node<String, String>>(), rootNodes).getRootNodes(), sameInstance(rootNodes));
    }
}
