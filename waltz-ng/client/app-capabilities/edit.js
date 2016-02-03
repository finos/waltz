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

import angular from 'angular';
import _ from 'lodash';


function filterAvailable(all, used) {
    const killList = _.map(used, 'capability.id');
    return _.filter(all, c => !_.contains(killList, c.id));
}

const controller = function(appCapabilityStore,
                            appStore,
                            capabilityStore,
                            ratingStore,
                            $stateParams,
                            $state,
                            $q) {

    const vm = this;
    const id = Number($stateParams.id);


    const model = {
        appCapabilities: [],
        availableCapabilities: [],
        allCapabilities: [],
        capabilityUsage: {}
    };


    const calculateAvailableCapabilities = () => {
        model.availableCapabilities = filterAvailable(model.allCapabilities, model.appCapabilities);
    };


    const clearForm = () => {
        this.addForm.capability = null;
    };


    const add = (newCapability) => {
        if (!_.any(model.capabilities, c => c.id === newCapability.id)) {
            return appCapabilityStore
                .addCapability(id, newCapability.id)
                .then(() => {
                    model.appCapabilities.push({
                        capability: newCapability,
                        applicationReference: {
                            id: id,
                            kind: 'APPLICATION'
                        },
                        description: newCapability.description,
                        primary: false
                    });
                    calculateAvailableCapabilities();
                    clearForm();
                });
        } else {
            return Promise.resolve();
        }
    };


    const remove = (capabilityId) => {
        if (model.capabilityUsage[capabilityId]) {
            alert('Cannot remove capability as it has ratings.');
        } else {
            appCapabilityStore
                .removeCapability(id, capabilityId)
                .then(() => {
                    model.appCapabilities = _.reject(model.appCapabilities, ac => ac.capability.id === capabilityId );
                    calculateAvailableCapabilities();
                });
        }
    };


    const appPromise = appStore
        .getById(id);

    const appCapabilityPromise = appCapabilityStore
        .findCapabilitiesByApplicationId(id);

    const capabilityPromise = capabilityStore
        .findAll();

    $q.all([capabilityPromise, appCapabilityPromise, appPromise])
        .then(([capabilities, appCapabilities, app]) => {
            model.allCapabilities = capabilities;
            const capabilitiesById = _.indexBy(capabilities, 'id');

            model.appCapabilities = _.map(appCapabilities, ac => {
                return {
                    original: true,
                    capability: capabilitiesById[ac.capabilityId],
                    application: app,
                    primary: ac.primary
                };
            });
            vm.application = app;
        })
        .then( () => calculateAvailableCapabilities())
        .then( () => ratingStore.findByParent('APPLICATION', id))
        .then(ratings => {
            model.capabilityUsage = _.foldl(
                ratings,
                (acc, r) => { acc[r.capability.id] = true; return acc; },
                {});
        });



    this.model = model;
    this.remove = remove;
    this.add = add;

    this.addForm = {};

    this.togglePrimaryCapability = (capability) => {
        const appCapability = _.find(model.appCapabilities, ac => ac.capability.id == capability.id);
        appCapability.primary = !appCapability.primary;
        appCapabilityStore.setIsPrimary(id, capability.id, appCapability.primary);
    };

    this.loadSuggestions = () => {
        appCapabilityStore.findAssociatedCapabilitiesByApplicationId(id)
            .then(suggestions => this.suggestions = suggestions);
    };

    this.addSuggestion = suggestion => {
        const capability = _.findWhere(model.allCapabilities, { id: suggestion.id } );
        if (!capability) {
            console.warn('Could not find capability for suggestion: ', suggestion);
            return;
        }
        this.add(capability).then(() => this.loadSuggestions());
    };

    this.mkPopoverHtml = (suggestion) =>
        '<ul class="list-unstyled">'
        + suggestion.values.map(a => `<li>- ${a.name}</li>`).join('')
        + '</ul>';
};

controller.$inject = [
    'AppCapabilityStore',
    'ApplicationStore',
    'CapabilityStore',
    'RatingStore',
    '$stateParams',
    '$state',
    '$q'
];


export default {
    template: require('./edit.html'),
    controller,
    controllerAs: 'ctrl'
};
