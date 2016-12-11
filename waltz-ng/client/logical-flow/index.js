
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

    const module = angular.module('waltz.logical.flow', []);

    module
        .config(require('./routes'));

    require('./directives')(module);
    require('./components')(module);

    module
        .service('LogicalFlowStore', require('./services/logical-flow-store'))
        .service('LogicalFlowViewService', require('./services/logical-flow-view-service'))
        .service('LogicalFlowUtilityService', require('./services/logical-flow-utility'));

    return module.name;
};
