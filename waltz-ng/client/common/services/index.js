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

import _ from 'lodash';

import BaseLookupService from './BaseLookupService';
import {
    applicationKindDisplayNames,
    authSourceRatingNames,
    bookmarkNames,
    capabilityRatingNames,
    involvementKindNames,
    lifecyclePhaseDisplayNames,
    orgUnitKindNames,
    severityNames,
    entityNames,
    assetCostKindNames
} from './display_names';


import {
    bookmarkIconNames,
    entityIconNames,
    severityIconNames
} from './icon_names';


const displayNameService = new BaseLookupService();
const iconNameService = new BaseLookupService();
const descriptionService = new BaseLookupService();

export default (module) => {
    module.service('WaltzDisplayNameService', () => displayNameService);
    module.service('WaltzIconNameService', () => iconNameService);
    module.service('WaltzDescriptionService', () => descriptionService);

    displayNameService.register('applicationKind', applicationKindDisplayNames);
    displayNameService.register('bookmark', bookmarkNames);
    displayNameService.register('involvementKind', involvementKindNames);
    displayNameService.register('lifecyclePhase', lifecyclePhaseDisplayNames);
    displayNameService.register('orgUnitKind', orgUnitKindNames);
    displayNameService.register('rating', authSourceRatingNames);
    displayNameService.register('severity', severityNames);
    displayNameService.register('capabilityRating', capabilityRatingNames);
    displayNameService.register('entity', entityNames);
    displayNameService.register('assetCost', assetCostKindNames);

    iconNameService.register('bookmark', bookmarkIconNames);
    iconNameService.register('entity', entityIconNames);
    iconNameService.register('severity', severityIconNames);

    module.run([
        'DataTypesDataService',
        (DataTypesDataService) =>
            DataTypesDataService
                .findAll()
                .then(results => {
                    const indexed = _.indexBy(results, 'code');
                    displayNameService.register('dataType', _.mapValues(indexed, 'name'));
                    descriptionService.register('dataType', _.mapValues(indexed, 'description'));
                })
    ]);

};
