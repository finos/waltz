import {CORE_API} from "../common/services/core-api-utils";
import {mkAuthoritativeRatingColorScale} from "../common/colors";
import _ from "lodash";


export function loadRatingColorScale(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => mkAuthoritativeRatingColorScale(
            _.filter(
                r.data,
                d => d.type === 'AuthoritativenessRating')));
}