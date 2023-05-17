import template from "./navbar-viewpoints.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {lastViewedMeasurableCategoryKey} from "../../../user";
import {initialiseData} from "../../../common";
import {isDescendant} from "../../../common/browser-utils";

const bindings = {}

const initialState = {
    showDropdown: false
}

const ESCAPE_KEYCODE = 27;

function controller($scope, $element, $document, $timeout, $q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const categoriesPromise = serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll, [])
        .then(r => r.data);

    const preferencesPromise = serviceBroker
        .execute(
            CORE_API.UserPreferenceStore.findAllForUser, [])
        .then(r => r.data);

    $q.all([categoriesPromise, preferencesPromise])
        .then(([categories, preferences]) => {

            const lastViewedCategory = _.find(preferences, d => d.key === lastViewedMeasurableCategoryKey);
            const firstCategory = _.first(categories);

            const catId = !_.isNil(lastViewedCategory)
                ? Number(lastViewedCategory.value)
                : firstCategory.id;

            vm.visibleCategory = _.find(categories, d => d.id === catId) || firstCategory;
            vm.categories = categories;
        });

    vm.selectCategory = (categoryId) => {
        vm.visibleCategory = _.find(vm.categories, d => d.id === categoryId);
        vm.showDropdown = false;
        serviceBroker
            .execute(
                CORE_API.UserPreferenceStore.saveForUser,
                [{key: lastViewedMeasurableCategoryKey, value: categoryId}]);
    }

    vm.toggleVisibility = () => {
        vm.showDropdown = !vm.showDropdown;
        managePageClicks();
    }

    const documentClick = (e) => {
        const element = $element[0];
        if (!isDescendant(element, e.target)) {
            $scope.$applyAsync(() => vm.showDropdown = false);
            $document.off("click", documentClick);
            $element.off("keydown", onOverlayKeypress);
        }
    };

    const onOverlayKeypress = (evt) => {
        if (evt.keyCode === ESCAPE_KEYCODE) {
            vm.showDropdown = false;
        }
    };

    const managePageClicks = () => {
        if (vm.showDropdown) {
            $timeout(() => $document.on("click", documentClick), 200);
            $timeout(() => $element.on("keydown", onOverlayKeypress), 200);
        } else {
            $document.off("click", documentClick);
            $element.off("keydown", onOverlayKeypress);
        }
    };

    vm.$onDestroy = () => {
        $document.off("click", documentClick);
        $element.off("keydown", onOverlayKeypress);
    }

}


controller.$inject = [
    "$scope",
    "$element",
    "$document",
    "$timeout",
    "$q",
    "ServiceBroker"
];

const component = {
    bindings,
    controller,
    template
}


export default {
    id: "waltzNavbarViewpoints",
    component
}