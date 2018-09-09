import _ from "lodash";
import {isEmpty, randomPick} from "../../../common";


const sourceRatings = ["R", "R", "A", "A", "G", "Z", "X" , "X"];
const targetRatings = ["G", "G", "G", "X", "X"];


export function mkRandomNode() {
    const t = _.random(0, 10000000);
    return {
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
}


export function mkRandomNodes() {
    const howMany = _.random(1, 16);
    return _.map(_.range(0, howMany), () => mkRandomNode());
}


export function mkRandomRowData(numCols = 3) {
    return _.map(_.range(0, numCols), () => mkRandomNodes());
}


export function filterData(data, qry) {
    if (isEmpty(qry)) {
        return data;
    }

    const nodeMatchFn = n => {
        const node = n.node;
        const nodeName = node.name.toLowerCase();
        const nodeExtId = node.externalId.toLowerCase();

        const ci = n.changeInitiative;
        const ciName = ci ? ci.name.toLowerCase() : "";
        const ciExtId = ci ? ci.externalId.toLowerCase() : "";

        const searchTargetStr = `${nodeName} ${nodeExtId} ${ ciName } ${ciExtId}`;
        return searchTargetStr.indexOf(qry) > -1;
    };

    const filterNodeGridFn = nodeGrid => _.filter(nodeGrid, nodeMatchFn);

    // console.log("filterData", { data, qry });
    return _.map(data, filterNodeGridFn);
}

