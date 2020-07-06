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

import _ from "lodash";
import {initialiseData} from "../../../common";
import template from "./favourites-button.html";
import {CORE_API} from "../../services/core-api-utils";

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    isFavourite: false,
    isReadOnly: false
};


function controller(serviceBroker, notification) {
    const vm = initialiseData(this, initialState);

    function loadFavourites() {
        return serviceBroker
            .loadViewData(CORE_API.FavouritesStore.getFavouritesGroupEntries)
            .then(r => vm.favourites = r.data)
    }

    vm.$onInit = () => {
        loadFavourites()
            .then(() => {
                const favourite = _.find(vm.favourites, d => d.id === vm.parentEntityRef.id);
                vm.isFavourite = !_.isUndefined(favourite);
                vm.isReadOnly = _.get(favourite, 'isReadOnly', false);
            });
    };

    vm.addFavourite = () => {
        serviceBroker
            .execute(CORE_API.FavouritesStore.addApplication, [vm.parentEntityRef.id])
            .then(() => {
                notification.success("Added to favourites");
                vm.isFavourite = true;
            })
    };

    vm.removeFavourite = () => {

        if (vm.isReadOnly){
            notification.error("This app is a direct involvement and cannot be removed from favorites")
        } else {
            serviceBroker
                .execute(CORE_API.FavouritesStore.removeApplication, [vm.parentEntityRef.id])
                .then(() => {
                    notification.info("Removed from favourites");
                    vm.isFavourite = false;
                })
        }
    }
}


controller.$inject = [
    "ServiceBroker",
    "Notification"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzFavouritesButton"
};
