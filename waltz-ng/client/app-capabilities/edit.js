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
    const killList = _.map(used, 'capabilityReference.id');
    return _.filter(all, c => !_.contains(killList, c.id));
}

const controller = function(appCapabilityStore,
                            appStore,
                            capabilityStore,
                            ratingStore,
                            $stateParams,
                            $state,
                            $q) {

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
                        capabilityReference: {
                            id: newCapability.id,
                            name: newCapability.name,
                            kind: 'CAPABILITY'
                        },
                        applicationReference: {
                            id: id,
                            kind: 'APPLICATION'
                        },
                        description: newCapability.description,
                        isPrimary: false
                    });
                    calculateAvailableCapabilities();
                    clearForm();
                });
        } else {
            return Promise.resolve();
        }
    };


    const remove = (c) => {
        if (model.capabilityUsage[c.id]) {
            alert('Cannot remove capability as it has ratings');
        } else {
            appCapabilityStore
                .removeCapability(id, c.id)
                .then(() => {
                    model.appCapabilities = _.reject(model.appCapabilities, ac => ac.capabilityReference.id === c.id );
                    calculateAvailableCapabilities();
                });
        }
    };


    appStore
        .getById(id)
        .then(app => this.application = app);


    const appCapabilityPromise = appCapabilityStore
        .findCapabilitiesByApplicationId(id);


    const capabilityPromise = capabilityStore
        .findAll();


    appCapabilityPromise
        .then(current => {
            model.appCapabilities = _.map(angular.copy(current), c => ( {...c, original: true }));
        });


    capabilityPromise
        .then(cs => model.allCapabilities = cs);

    $q.all([capabilityPromise, appCapabilityPromise])
        .then( () => calculateAvailableCapabilities())
        .then( () => ratingStore.findByParent('APPLICATION', id))
        .then(ratings => {
            model.capabilityUsage = _.foldl(ratings, (acc, r) => { acc[r.capability.id] = true; return acc; }, {});
        });


    this.model = model;
    this.remove = remove;
    this.add = add;

    this.addForm = {};

    this.togglePrimaryCapability = (capability) => {
        console.log(capability)
        capability.isPrimary = !capability.isPrimary;
        appCapabilityStore.setIsPrimary(id, capability.capabilityReference.id, capability.isPrimary);
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
    'CapabilityDataService',
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
