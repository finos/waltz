import _ from "lodash";

import {CORE_API} from "../common/services/core-api-utils";
import {scaleOrdinal} from "d3-scale";

export function loadFlowClassificationRatings(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.FlowClassificationStore.findAll)
        .then(r => _.orderBy(r.data,["position", "displayName"]));
}

export function loadFlowClassificationColorScale(serviceBroker) {
    return loadFlowClassificationRatings(serviceBroker)
        .then(flowClassifications => scaleOrdinal()
            .domain(_.map(flowClassifications, d => d.code))
            .range(_.map(flowClassifications, d => d.color)));
}