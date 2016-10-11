import {buildHierarchies} from "../common";
import {buildPropertySummer} from "../common/tally-utils";


export function prepareDataTypeTree(dataTypes, tallies) {
    const dataTypesById = _.keyBy(dataTypes, 'id');

    const enrichWithDirectCounts = (tallies, keyName) => {
        _.each(tallies, t => {
            const dt = dataTypesById[t.dataType.id];
            if (dt) dt[keyName] = t.total;
        });
    };

    enrichWithDirectCounts(tallies, "dataFlowCount");

    const rootDataTypes = buildHierarchies(dataTypes);

    const dataFlowCountSummer = buildPropertySummer("dataFlowCount", "totalDataFlowCount", "childDataFlowCount");

    _.each(rootDataTypes, dataFlowCountSummer);

    return rootDataTypes;
}