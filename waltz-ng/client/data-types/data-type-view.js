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
import {initialiseData, populateParents} from "../common";


const initialState = {
    dataFlow: null,
    entityRef: null
};


function controller($stateParams,
                    dataTypes) {

    const vm = initialiseData(this, initialState);

    const dataTypeId = $stateParams.id;

    const dataType = _.find(populateParents(dataTypes), { id: dataTypeId });

    const appIdSelector = {
        entityReference: {
            kind: 'DATA_TYPE',
            id: dataTypeId
        },
        scope: 'CHILDREN'
    };

    vm.entityRef = appIdSelector.entityReference;

    vm.dataType = dataType;
}


controller.$inject = [
    '$stateParams',
    'dataTypes',
];


export default {
    template: require('./data-type-view.html'),
    controller,
    controllerAs: 'ctrl'
};
