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
import BaseLookupService from "./BaseLookupService";
import {
    applicationKindDisplayNames,
    assetCostKindNames,
    authSourceRatingNames,
    bookmarkNames,
    capabilityRatingNames,
    changeInitiativeNames,
    criticalityDisplayNames,
    endOfLifeStatusNames,
    entityNames,
    entityStatisticCategoryDisplayNames,
    investmentRatingNames,
    lifecyclePhaseDisplayNames,
    orgUnitKindNames,
    rollupKindNames,
    severityNames,
    usageKindDisplayNames
} from "./display_names";
import {
    bookmarkIconNames,
    booleanTypeIconNames,
    entityIconNames,
    entityStatisticCategoryIconNames,
    severityIconNames,
    ragIconNames,
    usageKindIconNames
} from "./icon_names";


const displayNameService = new BaseLookupService();
const iconNameService = new BaseLookupService();
const descriptionService = new BaseLookupService();

export default (module) => {
    module
        .service('WaltzDisplayNameService', () => displayNameService)
        .service('WaltzIconNameService', () => iconNameService)
        .service('WaltzDescriptionService', () => descriptionService);

    displayNameService.register('applicationKind', applicationKindDisplayNames);
    displayNameService.register('assetCost', assetCostKindNames);
    displayNameService.register('bookmark', bookmarkNames);
    displayNameService.register('capabilityRating', capabilityRatingNames);
    displayNameService.register('changeInitiative', changeInitiativeNames);
    displayNameService.register('criticality', criticalityDisplayNames);
    displayNameService.register('endOfLifeStatus', endOfLifeStatusNames);
    displayNameService.register('entity', entityNames);
    displayNameService.register('entityStatistic', entityStatisticCategoryDisplayNames);
    displayNameService.register('investmentRating', investmentRatingNames);
    displayNameService.register('lifecyclePhase', lifecyclePhaseDisplayNames);
    displayNameService.register('orgUnitKind', orgUnitKindNames);
    displayNameService.register('rating', authSourceRatingNames);
    displayNameService.register('rollupKind', rollupKindNames);
    displayNameService.register('severity', severityNames);
    displayNameService.register('usageKind', usageKindDisplayNames);

    iconNameService.register('bookmark', bookmarkIconNames);
    iconNameService.register('BOOLEAN', booleanTypeIconNames);
    iconNameService.register('entity', entityIconNames);
    iconNameService.register('entityStatistic', entityStatisticCategoryIconNames);
    iconNameService.register('severity', severityIconNames);
    iconNameService.register('rag', ragIconNames);
    iconNameService.register('usageKind', usageKindIconNames);


    function runner(dataTypeService,
                    involvementKindService) {
        dataTypeService
            .loadDataTypes()
            .then(results => {
                // DEPRECATED, should be byId
                const indexedByCode = _.keyBy(results, 'code');
                displayNameService.register('dataType', _.mapValues(indexedByCode, 'name'));
                descriptionService.register('dataType', _.mapValues(indexedByCode, 'description'));

                const indexedById = _.keyBy(results, 'id');
                displayNameService.register('dataType', _.mapValues(indexedById, 'name'));
                descriptionService.register('dataType', _.mapValues(indexedById, 'description'));
            });

        involvementKindService
            .loadInvolvementKinds()
            .then(results => {
                const indexedById = _.keyBy(results, 'id');
                displayNameService.register('involvementKind', _.mapValues(indexedById, 'name'));
                descriptionService.register('involvementKind', _.mapValues(indexedById, 'description'));
            });
    }

    runner.$inject = [
        'DataTypeService',
        'InvolvementKindService'
    ];

    module.run(runner);

};
