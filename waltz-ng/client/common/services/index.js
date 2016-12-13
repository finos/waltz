/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import BaseLookupService from "./BaseLookupService";
import {
    applicationKindDisplayNames,
    applicationRatingNames,
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
    lifecycleStatusNames,
    orgUnitKindNames,
    rollupKindNames,
    severityNames,
    usageKindDisplayNames,
    transportKindNames,
    frequencyKindNames,
    dataFormatKindNames
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
        .service('DisplayNameService', () => displayNameService)
        .service('IconNameService', () => iconNameService)
        .service('DescriptionService', () => descriptionService);

    displayNameService.register('applicationKind', applicationKindDisplayNames);
    displayNameService.register('applicationRating', applicationRatingNames);
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
    displayNameService.register('lifecycleStatus', lifecycleStatusNames);
    displayNameService.register('orgUnitKind', orgUnitKindNames);
    displayNameService.register('rating', authSourceRatingNames);
    displayNameService.register('rollupKind', rollupKindNames);
    displayNameService.register('severity', severityNames);
    displayNameService.register('usageKind', usageKindDisplayNames);
    displayNameService.register('transportKind', transportKindNames);
    displayNameService.register('frequencyKind', frequencyKindNames);
    displayNameService.register('dataFormatKind', dataFormatKindNames);

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
