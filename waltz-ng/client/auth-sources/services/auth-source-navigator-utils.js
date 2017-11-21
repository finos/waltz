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
import {buildHierarchies, getParents, groupHierarchyByKey, indexHierarchyByKey} from "../../common/hierarchy-utils";

/**
 * Given a set of mappings converts them into a standard format using
 * the provided helper methods.
 *
 * The output is: { domainId, app, rating }
 *
 * @param mappings
 * @param domainIdProvider
 * @param appProvider
 * @param ratingProvider
 */
function normalizeMappings(mappings = [],
                           domainIdProvider,
                           appProvider,
                           ratingProvider) {
    return _
        .chain(mappings)
        .map(m => {
            return {
                domainId: domainIdProvider(m),
                app: appProvider(m),
                rating: ratingProvider(m)
            }
        })
        .value();
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


/**
 * Determine the domain entities required to build a minimal (but complete) hierarchy
 * encompassing the given mappings.  It does this by finding all ancestors of domain
 * items mentioned in the mappings.
 *
 * @param allDomain
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

class AuthSourceNavigatorUtil {

    constructor(dataTypes = [],
                measurables = [],
                authSources = [],
                ratings = [],
                apps = []) {

        this.dataTypesByCode = _.keyBy(dataTypes, 'code');
        this.appsById = _.keyBy(apps, 'id');

        const authSourceMappings = normalizeMappings(
            authSources,
            m => this.dataTypesByCode[m.dataType].id,
            m => this.appsById[m.applicationReference.id],
            m => 'Z');
        const measurableMappings = normalizeMappings(
            ratings,
            m => m.measurableId,
            m => this.appsById[m.entityReference.id],
            m => m.rating);

        this.dataTypeDataSet = prepareDataSet(dataTypes, authSourceMappings);
        this.measurableDataSet = prepareDataSet(measurables, measurableMappings);
    }




}
