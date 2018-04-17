
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import {buildHierarchies, getParents, groupHierarchyByKey, indexHierarchyByKey} from "../../common/hierarchy-utils";
import _ from 'lodash';


function recalcDomain(dataSet, focusId) {
    const potentialParents = _
        .chain(dataSet.nodesByParent[focusId] || [])
        .sortBy('name')
        .value();

    return potentialParents;
}



function populateRelated(hierarchyData) {
    const popParents = (node, mapping) => {
        _.each(
            getParents(node),
            parent => {
                parent.mappings.heirs = _.unionBy(
                    parent.mappings.heirs,
                    [ mapping ],
                    m => m.app.id)
            });
    };

    const popChildren = (node, mapping) => {
        _.each(
            node.children,
            child => {
                child.mappings.inherited = _.unionBy(
                    child.mappings.inherited,
                    [ mapping ],
                    m => m.app.id);
                popChildren(child, mapping);
            });
    };

    const byId = hierarchyData.nodesById;
    const nodes = _.values(byId);
    _.chain(nodes)
        .flatMap(n => n.mappings.direct)
        .each(mapping => {
            const node = byId[mapping.domainId];
            popParents(node, mapping);
            popChildren(node, mapping);
        })
        .value();
}




function findAncestors(dataTypesById, dt) {
    const acc = [ ];
    let ptr = dt;
    while (ptr) {
        acc.push(ptr);
        ptr = dataTypesById[ptr.parentId];
    }
    return acc;
}


function mkHierarchyDataSet(requiredElems) {
    const hierarchy = buildHierarchies(requiredElems);
    const nodesById = indexHierarchyByKey(hierarchy, n => n.id);
    const nodesByParent = groupHierarchyByKey(hierarchy, n => n.parentId);

    return {
        hierarchy,
        nodesById,
        nodesByParent
    };
}

/**
 * Determine the domain entities required to build a minimal (but complete) hierarchy
 * encompassing the given mappings.  It does this by finding all ancestors of domain
 * items mentioned in the mappings.
 *
 * @param domainById
 * @param mappings
 */
function determineRequiredDomainEntities(domainById = [], mappings = []) {
    return _
        .chain(mappings)
        .map(m => domainById[m.domainId])
        .compact()
        .uniqBy('id')
        .flatMap(m => findAncestors(domainById, m))
        .uniqBy('id')
        .value();
}


/**
 * Given domain entities and mappings will:
 *   - filter the domain to only those elements required to satisfy the hier for the given mappings
 *   - construct a hier given the above filtered domain
 *   - annotate that hier with apps mentioned in mappings,
 *   - each domain node shows inherited, heirs and direct mappings
 *
 * The returned object looks like:
 * ```
 * {
 *      hierarchy: [ { id, parentId, mappings: { direct, inherited, heirs } } ],
        nodesById: { <domainId>: { id, parentId, mappings: ...} },
        nodesByParent: { <domainId> : [ { id, parentId, mappings: ... }, ... ] }
 * }
 * ```
 *
 * @param allDomainEntities
 * @param mappings
 * @returns {{hierarchy, nodesById, nodesByParent}}
 */
function prepareDataSet(allDomainEntities = [], mappings = []) {
    const mkInitMappings = () => ({
        mappings: {
            direct: [],
            inherited: [],
            heirs: []
        }
    });

    const domainEntities = _.map(
        allDomainEntities,
        d => Object.assign({}, d, mkInitMappings()));

    const domainById = _.keyBy(domainEntities, 'id');

    _.chain(mappings)
        .filter(m => domainById[m.domainId])
        .each(m => domainById[m.domainId].mappings.direct.push(m))
        .value();

    const requiredDomainEntities = determineRequiredDomainEntities(domainById, mappings);
    const hierarchyData = mkHierarchyDataSet(requiredDomainEntities);

    populateRelated(hierarchyData);

    return hierarchyData;
}


// -- CLASS ---

export default class HierarchicalAxis {

    /**
     * rawData should look like:
     * ```
     * {
     *    domain: [ { id, parentId, ... } ],
     *    mappings: [ { domainId, app: { id, ... }, rating } ]
     * }
     * ```
     *
     * @param rawData
     */
    constructor(rawData) {
        this.dataSet = prepareDataSet(
            rawData.domain,
            rawData.mappings);

        this.current = {
            active: null,
            domain: recalcDomain(this.dataSet, null),
            parents: []
        };
    }

    focus(id) {
        const domain = recalcDomain(this.dataSet, id);
        if (domain.length === 0) {
            console.log('cannot focus on domain id: ', {id})
            return;
        }
        const active = this.dataSet.nodesById[id];
        this.current = {
            domain,
            active,
            parents: getParents(active),
        };
    }
}

