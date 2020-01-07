/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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
import attestationSummarySection from './components/section/attestation-summary-section';
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
        attestationSummarySection,
        inlineLogicalFlowPanel,
        inlineMeasurableRating,
        inlinePhysicalFlowPanel
    ]);

    return module.name;
};
