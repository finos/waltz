import _ from "lodash";
import {isEmpty, randomPick} from "../../../common";



export function filterData(data, qry) {
    const origData = _.cloneDeep(data);

    if (isEmpty(qry)) {
        return origData;
    }

    const nodeMatchFn = n => {
        return n.searchTargetStr.indexOf(qry) > -1;
    };

    const filterNodeGridFn = nodeGrid => _.filter(nodeGrid, nodeMatchFn);
    const filterRowFn = nodeGrids =>  _.map(nodeGrids, filterNodeGridFn);
    const filteredData = _.map(data, row => filterRowFn(row));

    return filteredData
}


export function enrichDatumWithSearchTargetString(datum) {
    const node = datum.node;
    const nodeName = node.name.toLowerCase();
    const nodeExtId = node.externalId.toLowerCase();

    const ci = datum.changeInitiative;
    const ciName = ci ? ci.name.toLowerCase() : "";
    const ciExtId = ci ? ci.externalId.toLowerCase() : "";

    const searchTargetStr = `${nodeName} ${nodeExtId} ${ ciName } ${ciExtId}`;

    return Object.assign({}, datum, { searchTargetStr });
}


// --- TEST DATA GENERATORS


const sourceRatings = ["R", "R", "A", "A", "G", "Z", "X" , "X"];
const targetRatings = ["R", "A", "G", "G", "G", "X", "X"];


function mkRandomDeltaNode() {
    const t = _.random(0, 10000000);
    const node = {
        id: t,
        node: {
            name: `App ${t}`,
            externalId: `${t}-1`,
            description: "about test app"
        },
        change: {
            base: {
                rating: randomPick(sourceRatings)
            },
            target: {
                rating: randomPick(targetRatings)
            }
        },
        changeInitiative: t % 2
            ? {name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
            : null
    };

    return enrichDatumWithSearchTargetString(node);
}



function mkRandomStaticNode() {
    const t = _.random(0, 10000000);
    const node = {
        id: t,
        node: {
            name: `App ${t}`,
            externalId: `${t}-1`,
            description: "about test app"
        },
        state: {
            rating: randomPick(targetRatings),
            comment: "test comment"
        }
    };

    return enrichDatumWithSearchTargetString(node);
}


function mkRandomNodes() {
    const howMany = _.random(1, 8);
    return _.map(_.range(0, howMany), () => mkRandomStaticNode());
}


export function mkRandomRowData(numCols = 3) {
    return _.map(_.range(0, numCols), () => mkRandomNodes());
}


export function mkRandomMeasurable(idx, desc) {
    return {
        id: `${desc}-${idx}`,
        name: `${desc}: ${idx} : abcdefghijklmnopqrstuvwxyz`,
    };
}
