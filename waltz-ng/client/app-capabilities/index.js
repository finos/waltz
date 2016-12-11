
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


export default () => {

    const module = angular.module('waltz.app.capabilities', []);


    module
        .config(require('./routes.js'));

    module
        .service('AppCapabilityStore', require('./services/app-capability-store'));

    module
        .component('waltzAppRatingTabgroupSection', require('./components/app-rating-tabgroup-section/app-rating-tabgroup-section'))
        .component('waltzAppCapabilityEditor', require('./components/app-capability-editor/app-capability-editor'))
        .component('waltzAppCapabilityPicker', require('./components/app-capability-picker/app-capability-picker'))
        .component('waltzAppCapabilityTable', require('./components/app-capability-table/app-capability-table'))
        .component('waltzAppCapabilityUsageEditor', require('./components/app-capability-usage-editor/app-capability-usage-editor'));

    require('./directives')(module);


    return module.name;
};
