import {buildHierarchies} from "../common";
import {buildPropertySummer} from "../common/tally-utils";


export function prepareDataTypeTree(dataTypes, tallies) {
    const dataTypesByCode = _.keyBy(dataTypes, 'code');

    const enrichWithDirectCounts = (tallies, keyName) => {
        _.each(tallies, t => {
            const dt = dataTypesByCode[t.id];
            if (dt) dt[keyName] = t.count;
        });
    };

    enrichWithDirectCounts(tallies, "dataFlowCount");

    const rootDataTypes = buildHierarchies(dataTypes);

    const dataFlowCountSummer = buildPropertySummer("dataFlowCount", "totalDataFlowCount", "childDataFlowCount");

    _.each(rootDataTypes, dataFlowCountSummer);

    return rootDataTypes;
}