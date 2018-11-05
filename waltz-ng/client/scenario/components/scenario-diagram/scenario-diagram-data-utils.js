import _ from "lodash";
import {isEmpty, randomPick} from "../../../common/index";
import {toOffsetMap} from "../../../common/list-utils";


export function filterData(data, qry) {
    const origData = _.cloneDeep(data);

    if (isEmpty(qry)) {
        return origData;
    }

    qry = qry.toLowerCase();

    const nodeMatchFn = n => {
        return _.get(n, ["searchTargetStr"], "").indexOf(qry) > -1;
    };

    const filterNodeGridFn = nodeGrid => _.filter(nodeGrid, nodeMatchFn);
    const filterRowFn = nodeGrids =>  _.map(nodeGrids, filterNodeGridFn);
    return _.map(data, filterRowFn);
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


function prepareAxisHeadings(scenarioDefinition, measurablesById, hiddenAxes = []) {
    const hiddenAxisIds = _.map(hiddenAxes, 'id');
    return _.chain(scenarioDefinition.axisDefinitions)
        .filter(d => !_.includes(hiddenAxisIds, d.domainItem.id))
        .map(d => {
            const measurable = measurablesById[d.domainItem.id];
            return {
                id: measurable.id,
                name: measurable.name,
                description: measurable.description,
                axisOrientation: d.axisOrientation,
                position: d.position,
                data: measurable
            };
        })
        .orderBy(d => d.position)
        .groupBy(d => d.axisOrientation)
        .value();
}





export function prepareData(scenarioDefinition, applications = [], measurables = [], hiddenAxes = []) {
    const applicationsById = _.keyBy(applications, "id");
    const measurablesById = _.keyBy(measurables, "id");
    const axisHeadings = prepareAxisHeadings(scenarioDefinition, measurablesById, hiddenAxes);

    const columnHeadings = axisHeadings["COLUMN"] || [];
    const rowHeadings = axisHeadings["ROW"] || [];

    const colOffsets = toOffsetMap(columnHeadings);
    const rowOffsets = toOffsetMap(rowHeadings);

    const baseRowData = _.times(rowHeadings.length, () => _.times(columnHeadings.length, () => []));

    const rowData = _.reduce(scenarioDefinition.ratings, (acc, d) => {
        const rowId = d.row.id;
        const columnId = d.column.id;
        const appId = d.item.id;
        const id = `${appId}_${rowId}_${columnId}`;

        const app = applicationsById[appId];

        const rowOffset = rowOffsets[rowId];
        const colOffset = colOffsets[columnId];

        const row = acc[rowOffset] || [];
        const col = row[colOffset] || [];

        const domainCoordinates = {
            row: measurablesById[d.row.id],
            column: measurablesById[d.column.id]
        };

        const nodeData = {
            id ,
            node: Object.assign({}, app, { externalId: app.assetCode }),
            domainCoordinates,
            state: {
                rating: d.rating,
                comment: d.description
            },
            searchTargetStr: `${app.name} ${app.assetCode}`.toLowerCase()
        };

        row[colOffset] = _.concat(col, [nodeData]);
        acc[rowOffset] = row;

        return acc;
    }, baseRowData);

    return {
        columnHeadings,
        rowHeadings,
        rowData
    };
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
