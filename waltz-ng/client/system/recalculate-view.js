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

const template = require('./recalculate-view.html');


function controller(notification,
                    attestationStore,
                    authSourceStore,
                    complexityStore,
                    dataTypeUsageStore) {
    const vm = this;


    vm.recalcFlowRatings = () => {
        notification.info('Flow Ratings recalculation requested');
        authSourceStore
            .recalculateAll()
            .then(() => notification.success('Flow Ratings recalculated'));
    };

    vm.recalcDataTypeUsages = () => {
        notification.info('Data Type Usage recalculation requested');
        dataTypeUsageStore
            .recalculateAll()
            .then(() => notification.success('Data Type Usage recalculated'));
    };

    vm.recalcComplexity = () => {
        notification.info('Complexity recalculation requested');
        complexityStore
            .recalculateAll()
            .then(() => notification.success('Complexity recalculated'));
    };

    vm.recalcFlowDiagramAttestations = () => {
        notification.info('Flow diagram recalculation requested');
        attestationStore
            .recalculateForFlowDiagrams()
            .then(() => notification.success('Flow diagram attestation recalculated'));
    };


    vm.recalcLogicalFlowDecoratorAttestations = () => {
        notification.info('Logical flow decorator attestation recalculation requested');
        attestationStore
            .recalculateForLogicalFlowDecorators()
            .then(() => notification.success('Logical flow decorator attestation recalculated'));
    };
}


controller.$inject = [
    'Notification',
    'AttestationStore',
    'AuthSourcesStore',
    'ComplexityStore',
    'DataTypeUsageStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;