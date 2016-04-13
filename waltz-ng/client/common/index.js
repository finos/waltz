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
        .indexBy('id')
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

