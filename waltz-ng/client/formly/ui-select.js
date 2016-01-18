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

export default (formlyConfig) => {
    // NOTE: This next line is highly recommended. Otherwise Chrome's autocomplete will appear over your options!
    formlyConfig.extras.removeChromeAutoComplete = true;
    formlyConfig.setType({
        name: 'async-ui-select',
        extends: 'select',
        templateUrl: 'async-ui-select-type.html'
    });

    formlyConfig.setType({
        name: 'ui-select',
        extends: 'select',
        template: '<ui-select ng-model="model[options.key]" theme="bootstrap" ng-required="{{to.required}}" ng-disabled="{{to.disabled}}" reset-search-input="false"> <ui-select-match placeholder="{{to.placeholder}}"> {{$select.selected[to.labelProp || \'name\']}} </ui-select-match> <ui-select-choices group-by="to.groupBy" repeat="option[to.valueProp || \'value\'] as option in to.options | filter: $select.search"> <div ng-bind-html="option[to.labelProp || \'name\'] | highlight: $select.search"></div> </ui-select-choices> </ui-select>'
    });

    formlyConfig.setType({
        name: 'ui-select-select2',
        extends: 'ui-select',
        template: '<ui-select ng-model="model[options.key]" theme="select2" ng-required="{{to.required}}" ng-disabled="{{to.disabled}}" reset-search-input="false"> <ui-select-match placeholder="{{to.placeholder}}"> {{$select.selected[to.labelProp || \'name\']}} </ui-select-match> <ui-select-choices group-by="to.groupBy" repeat="option[to.valueProp || \'value\'] as option in to.options | filter: $select.search"> <div ng-bind-html="option[to.labelProp || \'name\'] | highlight: $select.search"></div> </ui-select-choices> </ui-select>'
    });

    formlyConfig.setType({
        name: 'ui-select-selectize',
        extends: 'ui-select',
        template: '<ui-select ng-model="model[options.key]" theme="selectize" ng-required="{{to.required}}" ng-disabled="{{to.disabled}}" reset-search-input="false"> <ui-select-match placeholder="{{to.placeholder}}"> {{$select.selected[to.labelProp || \'name\']}} </ui-select-match> <ui-select-choices group-by="to.groupBy" repeat="option[to.valueProp || \'value\'] as option in to.options | filter: $select.search"> <div ng-bind-html="option[to.labelProp || \'name\'] | highlight: $select.search"></div> </ui-select-choices> </ui-select>'
    });
};
