import _ from "lodash";

import {CORE_API} from "../common/services/core-api-utils";
import {mkAuthoritativeRatingColorScale} from "../common/colors";

export function loadAuthSourceRatings(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => _
            .chain(r.data)
            .filter(d => d.type === "AuthoritativenessRating")
            .orderBy(["position", "displayName"])
            .value());
}

export function loadRatingColorScale(serviceBroker) {
    return loadAuthSourceRatings(serviceBroker)
        .then(mkAuthoritativeRatingColorScale);
}