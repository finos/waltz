/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from 'lodash';


/**
 *  Given a set of nodes with id and parentId constructs a 'searchStr' property for each
 *  node which is the concatenation of a specified property (attr) of all the nodes
 *  parent nodes.
 */
export function prepareSearchNodes(nodes = [], attr = 'name', parentKey = 'parentId') {
    const nodesById = _.keyBy(nodes, 'id');

    return _.map(nodes, n => {
        let ptr = n;
        let searchStr = '';
        const nodePath = [];
        while (ptr) {
            nodePath.push(ptr);
            searchStr += (ptr[attr] || '') + ' ';
            const parentId = ptr[parentKey];
            ptr = nodesById[parentId];
        }
        return {
            searchStr: searchStr.toLowerCase(),
            nodePath
        };
    });
}


/**
 * The given `termStr` will be tokenised and
 * all nodes (given in `searchNodes`) which contain all tokens
 * will be returned (de-duped).
 *
 * Use `prepareSearchNodes` to prepare the search nodes.
 * @param termStr
 * @param searchNodes
 */
export function doSearch(termStr = '', searchNodes = []) {
    const terms = _.split(termStr.toLowerCase(), /\W+/);

    return _
        .chain(searchNodes)
        .filter(sn => {
            const noTerms = termStr.trim().length === 0;
            const allMatch = _.every(terms, t => sn.searchStr.indexOf(t) >=0)
            return noTerms || allMatch;
        })
        .flatMap('nodePath')
        .uniqBy(n => n.id)
        .value();
}


/**
 * Given data that looks like:
 *
 *    [ { id: "",  parentId: ?, ... } , .. ]
 *
 * Gives back an array of top level objects which have children
 * nested in them, the result looks something like:
 *
 *    [ id: "", parentId : ?, parent : {}?, children : [ .. ], ... },  .. ]
 *
 * @param nodes
 * @returns {Array}
 */
export function populateParents(nodes) {
    const byId = _.chain(_.cloneDeep(nodes))
        .map(u => _.merge(u, { children: [], parent: null }))
        .keyBy('id')
        .value();

    _.each(_.values(byId), u => {
        if (u.parentId) {
            const parent = byId[u.parentId];
            if (parent) {
                parent.children.push(u);
                u.parent = parent;
            }
        }
    });

    return _.values(byId);
}


export function buildHierarchies(nodes) {
    // only give back root element/s
    return _.reject(populateParents(nodes), n => n.parent);
}


/**
 The wix tree widget does deep comparisons.
 Having parents as refs therefore blows the callstack.
 This method will replace refs with id's.
 */
export function switchToParentIds(treeData = []) {
    _.each(treeData, td => {
        td.parent = td.parent ? td.parent.id : null;
        switchToParentIds(td.children);
    });
    return treeData;
}


export function findNode(nodes = [], id) {
    const found = _.find(nodes, { id });
    if (found) return found;

    for(let i = 0; i < nodes.length; i++) {
        const f = findNode(nodes[i].children, id);
        if (f) return f;
    }

    return null;
}


export function getParents(node) {
    if (! node) return [];

    let ptr = node.parent;

    const result = [];

    while (ptr) {
        result.push(ptr);
        ptr = ptr.parent;
    }

    return result;
}
