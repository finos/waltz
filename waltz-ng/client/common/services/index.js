/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
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
    surveyInstanceStatusNames,
    surveyRunStatusNames,
    surveyTemplateStatusNames,
    usageKindDisplayNames,
    transportKindNames,
    frequencyKindNames,
    dataFormatKindNames
} from "./display-names";
import {
    bookmarkIconNames,
    booleanTypeIconNames,
    entityIconNames,
    entityStatisticCategoryIconNames,
    severityIconNames,
    ragIconNames,
    usageKindIconNames
} from "./icon-names";


const displayNameService = new BaseLookupService();
const iconNameService = new BaseLookupService();
const descriptionService = new BaseLookupService();


function loadFromServer(dataTypeService,
                        involvementKindService,
                        measurableCategoryStore) {
    dataTypeService
        .loadDataTypes()
        .then(results => {
            // DEPRECATED, should be byId
            const indexedByCode = _.keyBy(results, 'code');
            const indexedById = _.keyBy(results, 'id');

            displayNameService
                .register('dataType', _.mapValues(indexedByCode, 'name'))
                .register('dataType', _.mapValues(indexedById, 'name'))
                ;

            descriptionService
                .register('dataType', _.mapValues(indexedByCode, 'description'))
                .register('dataType', _.mapValues(indexedById, 'description'))
                ;
        });

    involvementKindService
        .loadInvolvementKinds()
        .then(results => {
            const indexedById = _.keyBy(results, 'id');
            displayNameService.register('involvementKind', _.mapValues(indexedById, 'name'));
            descriptionService.register('involvementKind', _.mapValues(indexedById, 'description'));
        });

    measurableCategoryStore
        .findAll()
        .then(results => {
            const indexedById = _.keyBy(results, 'id');
            displayNameService.register('measurableCategory', _.mapValues(indexedById, 'name'));
            descriptionService.register('measurableCategory', _.mapValues(indexedById, 'description'));
        })
}


export default (module) => {
    module
        .service('DisplayNameService', () => displayNameService)
        .service('IconNameService', () => iconNameService)
        .service('DescriptionService', () => descriptionService)
        ;

    displayNameService
        .register('applicationKind', applicationKindDisplayNames)
        .register('applicationRating', applicationRatingNames)
        .register('assetCost', assetCostKindNames)
        .register('bookmark', bookmarkNames)
        .register('capabilityRating', capabilityRatingNames)
        .register('changeInitiative', changeInitiativeNames)
        .register('criticality', criticalityDisplayNames)
        .register('dataFormatKind', dataFormatKindNames)
        .register('endOfLifeStatus', endOfLifeStatusNames)
        .register('entity', entityNames)
        .register('entityStatistic', entityStatisticCategoryDisplayNames)
        .register('frequencyKind', frequencyKindNames)
        .register('investmentRating', investmentRatingNames)
        .register('lifecyclePhase', lifecyclePhaseDisplayNames)
        .register('lifecycleStatus', lifecycleStatusNames)
        .register('orgUnitKind', orgUnitKindNames)
        .register('rating', authSourceRatingNames)
        .register('rollupKind', rollupKindNames)
        .register('severity', severityNames)
        .register('surveyInstanceStatus', surveyInstanceStatusNames)
        .register('surveyRunStatus', surveyRunStatusNames)
        .register('surveyTemplateStatus', surveyTemplateStatusNames)
        .register('usageKind', usageKindDisplayNames)
        .register('transportKind', transportKindNames)
        ;

    iconNameService
        .register('bookmark', bookmarkIconNames)
        .register('BOOLEAN', booleanTypeIconNames)
        .register('entity', entityIconNames)
        .register('entityStatistic', entityStatisticCategoryIconNames)
        .register('severity', severityIconNames)
        .register('rag', ragIconNames)
        .register('usageKind', usageKindIconNames)
        ;


    loadFromServer.$inject = [
        'DataTypeService',
        'InvolvementKindService',
        'MeasurableCategoryStore'
    ];

    module.run(loadFromServer);
};
