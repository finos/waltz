import _ from "lodash";
import ListView from "./pages/list/measurable-category-list.js";
import EditView from "./pages/edit/measurable-category-edit.js";
import {lastViewedMeasurableCategoryKey} from "../user/services/user-preference-service";


const baseState = {};


function bouncer($state, $stateParams, userPreferenceService, settingsService) {
    const go = id => $state.go("main.measurable-category.list", { id }, { location: "replace" });

    const attemptToRouteViaLastVisited = () => userPreferenceService
        .loadPreferences(true)
        .then(prefs => {
            const lastCategory = prefs[lastViewedMeasurableCategoryKey];
            if (_.get(lastCategory, "value", 0) > 0) {
                go(lastCategory.value);
            } else {
                attemptToRouteViaServerSetting();
            }
        });

    const attemptToRouteViaServerSetting = () => settingsService
        .findOrDefault("settings.measurable.default-category", null)
        .then(defaultCategoryId => {
            if (defaultCategoryId) {
                go(defaultCategoryId)
            } else {
                go(1);
            }
        });

    attemptToRouteViaLastVisited();
}

bouncer.$inject = ["$state", "$stateParams", "UserPreferenceService", "SettingsService"];


const bouncerState = {
    url: "measurable-category/",
    resolve: {
        bouncer
    }
};

const listState = {
    url: "measurable-category/{id:int}",
    views: {
        "content@": ListView
    }
};

const editState = {
    url: "measurable-category/{id:int}/edit",
    views: {
        "content@": EditView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.measurable-category", baseState)
        .state("main.measurable-category.index", bouncerState)
        .state("main.measurable-category.list", listState)
        .state("main.measurable-category.edit", editState);
}


setup.$inject = [
    "$stateProvider"
];


export default setup;