/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
import attestedKind from './components/attested-kind/attested-kind';
import attestationRunOverview from './components/run-overview/attestation-run-overview';
import attestationRecipients from './components/recipients/attestation-recipients';
import attestationSection from './components/section/attestation-section';
import inlineLogicalFlowPanel from './components/inline-logical-flow-panel/inline-logical-flow-panel';
import inlineMeasurableRating from './components/inline-measurable-rating-panel/inline-measurable-rating-panel';
import inlinePhysicalFlowPanel from './components/inline-physical-flow-panel/inline-physical-flow-panel';
import routes from './routes';


export default () => {
    const module = angular.module('waltz.attestation', []);

    module
        .config(routes);

    registerStores(module, [
        attestationInstanceStore,
        attestationRunStore
    ]);

    registerComponents(module, [
        attestationConfirmation,
        attestedKind,
        attestationRunOverview,
        attestationRecipients,
        attestationSection,
        inlineLogicalFlowPanel,
        inlineMeasurableRating,
        inlinePhysicalFlowPanel
    ]);

    return module.name;
};
