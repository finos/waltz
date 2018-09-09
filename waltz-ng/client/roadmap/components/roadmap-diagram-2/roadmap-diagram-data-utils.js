import _ from "lodash";
import {randomPick} from "../../../common";


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