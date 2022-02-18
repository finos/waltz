/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.common.hierarchy;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.ListUtilities;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


public class HierarchyUtilities {

    /**
     * Given a set of flat nodes, will construct a hierarchy and
     * return a tuple of a map of all nodes, and the collection
     * of root nodes.
     *
     * @param flatNodes collection of flat nodes which will be used to construct the forest
     * @param <T> type of the node data
     * @param <K> type of the node key
     * @return Forest built from the given flat nodes
     */
    public static <T, K> Forest<T, K> toForest(Collection<FlatNode<T, K>> flatNodes) {
        Collection<FlatNode<T, K>> sanitizedFlatNodes = sanitizeFlatNodes(flatNodes);

        List<K> rootNodeIds = sanitizedFlatNodes
                .stream()
                .filter(n -> ! n.getParentId().isPresent())
                .map(FlatNode::getId)
                .collect(Collectors.toList());

        Map<K, Node<T, K>> allById = sanitizedFlatNodes
                .stream()
                .collect(toMap(
                        FlatNode::getId,
                        n -> new Node<>(n.getId(), n.getData()),
                        (n1, n2) -> n1));

        sanitizedFlatNodes
                .stream()
                .filter(n -> n.getParentId().isPresent())
                .forEach(n -> {
                    Node<T, K> parent = allById.get(n.getParentId().get());
                    if (parent == null) {
                        // no parent, therefore must be a root which we aren't interested in
                        return;
                    }
                    Node<T, K> node = allById.get(n.getId());
                    parent.addChild(node);
                    node.setParent(parent);
                });

        Set<Node<T, K>> rootNodes = rootNodeIds
                .stream()
                .map(allById::get)
                .collect(Collectors.toSet());

        return new Forest<>(allById, rootNodes);
    }


    /**
     * Takes a collection of flat nodes and ensures that:
     *
     * - none of them have a parent id the same as their id
     *
     * A new collection of flat nodes is returned
     *
     * @param flatNodes  Collection of unsanitized flat nodes
     * @param <T>  The type of the node content
     * @param <K>  The type of the key used to reference nodes
     * @return  collection of sanitized flat nodes
     */
    private static <T, K> Collection<FlatNode<T, K>> sanitizeFlatNodes(Collection<FlatNode<T, K>> flatNodes) {
        return CollectionUtilities.map(
                flatNodes,
                n -> n.getParentId()
                        .filter(pId -> pId.equals(n.getId()))
                        .map(pId -> new FlatNode<>(n.getId(), Optional.empty(), n.getData()))
                        .orElse(n));
    }


    public static <T, K> boolean hasCycle(Forest<T, K> forest) {
        Checks.checkNotNull(forest, "forest must not be null");
        PMap<K, Node<T, K>> seen = HashTreePMap.empty();

        return forest
                .getAllNodes()
                .values()
                .stream()
                .anyMatch(node -> hasCycle(node, seen));
    }


    private static <T, K> boolean hasCycle(Node<T, K> node, PMap<K, Node<T, K>> seen) {
        if (seen.containsKey(node.getId())) {
            return true;
        }

        PMap<K, Node<T, K>> updated = seen.plus(node.getId(), node);

        return node.getChildren()
                .stream()
                .anyMatch(child -> hasCycle(child, updated));
    }


    /**
     * Returns a list of parent nodes, immediate parents first
     * @param startNode node to start from, not included in output
     * @param <T> type of the node data
     * @param <K> type of the node key
     * @return list of parents to this node (or empty list)
     */
    public static <T, K> List<Node<T, K>> parents(Node<T, K> startNode) {
        List<Node<T, K>> parents = ListUtilities.newArrayList();

        Node<T,K> parent = startNode.getParent();
        while (parent != null) {
            parents.add(parent);
            parent = parent.getParent();
        }

        return parents;
    }


    public static <T, K> Map<K, Integer> assignDepths(Forest<T, K> forest) {
        return assignDepths(forest.getRootNodes(), 1);
    }


    private static <T, K> Map<K, Integer> assignDepths(Collection<Node<T, K>> nodes,
                                                       int level) {
        Map<K, Integer> result = new HashMap<>();

        for (Node<T, K> node : nodes) {
            result.put(node.getId(), level);
            result.putAll(assignDepths(node.getChildren(), level + 1));
        }

        return result;
    }
}
