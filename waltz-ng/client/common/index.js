/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {nest} from "d3-collection";
import {checkIsEntityRef} from "./checks";


export function notEmpty(xs) {
    return ! _.isEmpty(xs);
}


export function isEmpty(xs) {
    return _.isEmpty(xs);
}


export function mkSafe(xs = []) {
    return _.isEmpty(xs) ? [] : xs;
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


export function flattenHiearchies(roots = [], acc) {
    roots.forEach(r => {
        acc.push(r);
        flattenHiearchies(r.children, acc);
    });
    return acc;
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


// https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
export function escapeRegexCharacters(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}


export function shallowDiff(newObj, oldObj) {
    const allKeys = _.union(_.keys(newObj), _.keys(oldObj));
    return _.chain(allKeys)
        .reject(k => _.isEqual(newObj[k], oldObj[k]))
        .map(k => ({
            field: k,
            oldValue: _(oldObj[k]).toString(),
            newValue: _(newObj[k]).toString()
        }))
        .value();
}


export function noop() {}


export function randomPick(xs) {
    if (!xs) throw new Error('Cannot pick from a null set of options');

    const choiceCount = xs.length - 1;
    const idx = Math.round(Math.random() * choiceCount);
    return xs[idx];
}


/**
 * Attempts to return the result of the given function.
 * If the function throws an exception the default value
 * will be returned
 *
 * @param fn
 * @param dflt
 * @returns {*}
 */
export function perhaps(fn, dflt) {
    try {
        return fn();
    } catch (e) {
        return dflt;
    }
}


export function numberFormatter(num, digits) {
    const si = [
        { value: 1E12, symbol: "T" },
        { value: 1E9,  symbol: "B" },
        { value: 1E6,  symbol: "M" },
        { value: 1E3,  symbol: "k" }
    ];

    for (let i = 0; i < si.length; i++) {
        if (num >= si[i].value) {
            return (num / si[i].value)
                    .toFixed(digits)
                    .replace(/\.?0+$/, "") + si[i].symbol;
        }
    }
    return num;
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


/**
 *
 * @param items - items to be searched
 * @param searchStr - query string to search for
 * @param searchFields - fields in the items to consider when searching
 * @returns {Array}
 */
export function termSearch(items = [], searchStr = '', searchFields = []) {
    const terms = searchStr.toLowerCase().split(/\W/);

    return _.filter(items, item => {
        const fields = _.isEmpty(searchFields)
            ? _.keys(item)
            : searchFields;

        const targetStr = _.chain(fields)
            .reject(field => field.startsWith('$') || _.isFunction(_.get(item, field)))
            .map(field => _.get(item, field))
            .join(' ')
            .value()
            .toLowerCase();

        return _.every(terms, term => targetStr.includes(term));
    });
}


/**
 * the d3 nest function aggregates using the property name 'values', this
 * function creates a copy of the data with the name 'count'.
 *
 * @param data
 * @returns {Array|*}
 */
function toCountData(data = []) {
    return _.map(
        data,
        d => ({
            key: d.key,
            count: d.value
        }));
}


export function toKeyCounts(items = [], fn = x => x) {
    if (! items) return [];
    return toCountData(nest()
        .key(fn)
        .rollup(d => d.length)
        .entries(items));
}


/**
 * Given an entity kind, this will return the matching
 * ui-router state name if avaialble.  Otherwise it
 * will throw an error.
 * @param kind
 * @returns String state name
 */
export function kindToViewState(kind) {
    if (kind === 'APPLICATION') {
        return "main.app.view";
    }
    if (kind === 'ACTOR') {
        return "main.actor.view";
    }
    if (kind === 'APP_GROUP') {
        return "main.app-group.view";
    }
    if (kind === 'CAPABILITY') {
        return "main.capability.view";
    }
    if (kind === 'DATA_TYPE') {
        return "main.data-type.view";
    }
    if (kind === 'ORG_UNIT') {
        return "main.org-unit.view";
    }
    if (kind === 'CHANGE_INITIATIVE') {
        return "main.change-initiative.view";
    }
    if (kind === 'ENTITY_STATISTIC') {
        return "main.entity-statistic.view";
    }
    if (kind === 'PROCESS') {
        return "main.process.view";
    }
    throw "Unable to convert kind: "+kind+ " to a ui-view state";
}


export function resetData(vm, initData = {}) {
    return Object.assign(vm, _.cloneDeep(initData));
}


export function initialiseData(vm, initData) {
    return _.defaultsDeep(vm, _.cloneDeep(initData));
}


export function stringToBoolean(string){
    switch(string.toLowerCase().trim()){
        case "true":
        case "yes":
        case "1":
            return true;
        case "false":
        case "no":
        case "0":
        case null:
        case undefined:
            return false;
        default:
            return Boolean(string);
    }
}


/**
 * Invokes a function and also passes in any provided arguments in order
 * e.g. invokeFunction(onClick, arg1, arg2)
 * @param fn
 * @returns {*}
 */
export function invokeFunction(fn) {
    if (_.isFunction(fn)) {
        const parameters = _.slice(arguments, 1);
        return fn(...parameters);
    }
    console.log("invokeFunction - attempted to invoke emtpy function: ", fn)
    return null;
}


/**
 * Creates a column def to render an entity link
 *
 * eg: usage: mkEntityLinkGridCell('Source', 'source', 'none')
 *
 * @param columnHeading column display name
 * @param entityRefField field name in grid data that stores the entity ref for which the link needs to be rendered
 * @param iconPlacement icon position, allowed values: left, right, none
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkEntityLinkGridCell(columnHeading, entityRefField, iconPlacement = 'left') {
    return {
        field: entityRefField + '.name',
        displayName: columnHeading,
        cellTemplate: `<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity['${entityRefField}']" icon-placement="'${iconPlacement}'"></waltz-entity-link></div>`
    };
}


/**
 * Creates a column def to render a link with an id parameter
 *
 * @param columnHeading column display name
 * @param displayField field name that stores the value to be displayed on the grid
 * @param linkIdField field name that stores the link id field
 * @param linkNavViewName navigation view name
 * @returns {{field: *, displayName: *, cellTemplate: string}}
 */
export function mkLinkGridCell(columnHeading, displayField, linkIdField, linkNavViewName) {
    return {
        field: displayField,
        displayName: columnHeading,
        cellTemplate: `<div class="ui-grid-cell-contents">\n<a ui-sref="${linkNavViewName} ({ id: row.entity.${linkIdField} })" ng-bind="COL_FIELD">\n</a>\n</div>`
    };
}


export function toEntityRef(obj) {
    const ref = {
        id: obj.id,
        kind: obj.kind,
        name: obj.name,
        description: obj.description
    };

    checkIsEntityRef(ref);

    return ref;
}


/**
 * Given a url, turns it to a domain name i.e. www.test.com/blah becomes www.test.com
 * if a mail link is supplied, i.e. mailto:mail@somewhere.com, this becomes mail@somehwere.com
 * @param url
 * @returns {*}
 */
export function toDomain(url) {
    let domain;
    //find & remove protocol (http, ftp, etc.) and get domain
    if (url.indexOf("://") > -1) {
        domain = url.split('/')[2];
    } else if(url.indexOf("mailto:") > -1) {
        domain = url.split('mailto:')[1];
    }
    else {
        domain = url.split('/')[0];
    }

    //find & remove port number
    domain = domain.split(':')[0];

    return domain;
}

