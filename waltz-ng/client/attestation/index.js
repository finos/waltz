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

import angular from 'angular';
import {registerComponents, registerStores} from "../common/module-utils";
import * as attestationInstanceStore from './services/attestation-instance-store';
import attestationRunStore from './services/attestation-run-store';
import attestationConfirmation from './components/confirmation/attestation-confirmation';
import attestationRunOverview from './components/run-overview/attestation-run-overview';
import attestationRecipients from './components/recipients/attestation-recipients';
import attestationSection from './components/section/attestation-section';
import routes from './routes';

export default () => {
    const module = angular.module('waltz.attestation', []);

    module
        .config(routes);

    module
        .service('AttestationStore', require('./services/attestation-store'));

    registerStores(module, [
        attestationInstanceStore,
        attestationRunStore
    ]);

    registerComponents(module, [
        attestationConfirmation,
        attestationRunOverview,
        attestationRecipients,
        attestationSection
    ]);

    return module.name;
};
