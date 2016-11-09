
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

export default (module) => {

    require('./directives')(module);

    module
        .config(require('./routes.js'))
        .service(
            'AppCapabilityStore',
            require('./services/app-capability-store'))
        .component('waltzAppRatingTabgroupSection', require('./components/app-rating-tabgroup-section/app-rating-tabgroup-section'))
        .component('waltzAppCapabilityEditor', require('./components/app-capability-editor/app-capability-editor'))
        .component('waltzAppCapabilityPicker', require('./components/app-capability-picker/app-capability-picker'))
        .component('waltzAppCapabilityTable', require('./components/app-capability-table/app-capability-table'));

};
