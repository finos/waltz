import _ from "lodash";


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


export function perhaps(fn, dflt) {
    try {
        return fn();
    } catch (e) {
        return dflt;
    }
}


export function numberFormatter(num, digits) {
    var si = [
        { value: 1E12, symbol: "T" },
        { value: 1E9,  symbol: "B" },
        { value: 1E6,  symbol: "M" },
        { value: 1E3,  symbol: "k" }
    ], i;
    for (i = 0; i < si.length; i++) {
        if (num >= si[i].value) {
            return (num / si[i].value).toFixed(digits).replace(/\.?0+$/, "") + si[i].symbol;
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
            .reject(field => field.startsWith('$') || _.isFunction(item[field]))
            .map(field => item[field])
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
function toCountData(data) {
    return _.map(data, d => ({ key: d.key, count: d.values }));
}


export function toKeyCounts(items = [], fn = x => x) {
    return toCountData(d3.nest()
        .key(fn)
        .rollup(d => d.length)
        .entries(items))
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
    if (kind === 'APP_GROUP') {
        return "main.app-group.view";
    }
    if (kind === 'CAPABILITY') {
        return "main.capability.view";
    }
    if (kind === 'ORG_UNIT') {
        return "main.org-unit.view";
    }
    if (kind === 'CHANGE_INITIATIVE') {
        return "main.change-initiative.view";
    }
    if (kind === 'PROCESS') {
        return "main.process.view";
    }
    throw "Unable to convert kind: "+kind+ " to a ui-view state";
}


export function initialiseData(vm, initData) {
    return Object.assign(vm, _.cloneDeep(initData));
}