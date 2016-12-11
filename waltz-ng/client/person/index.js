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

    const module = angular.module('waltz.person', []);

    module
        .config(require('./routes'))

    module
        .directive('waltzPersonSelector', require('./directives/person-selector'))
        .directive('waltzPersonLink', require('./directives/person-link'))
        .directive('waltzManagerList', require('./directives/manager-list'))
        .directive('waltzPersonDirectsList', require('./directives/person-directs-list'))
        .directive('waltzPersonSummary', require('./directives/person-summary'));

    module
        .service('PersonViewDataService', require('./person-view-data'))
        .service('PersonStore', require('./services/person-store'));

    return module.name;
};
