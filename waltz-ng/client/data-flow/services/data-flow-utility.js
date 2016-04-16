
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";

export default [
    () => {

        const enrich = (flows, flowEntities) => {
            const entitiesById = _.keyBy(flowEntities, 'id');

            return _.map(flows, f => ({
                ...f,
                sourceEntity: entitiesById[f.source.id],
                targetEntity: entitiesById[f.target.id]
            }));
        };

        const getDataTypes = (flows) => {
            return _.chain(flows)
                .map('dataType')
                .flatten()
                .uniq()
                .value();
        };


        return {
            enrich,
            getDataTypes
        };
    }
];
