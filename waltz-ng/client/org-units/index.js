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

import "angular-ui-router";


export default (module) => {

    require('./directives')(module);
    require('./templates')(module);

    module.config(require('./routes'));

    module.service('OrgUnitStore', require('./services/org-unit-store'));
    module.service('OrgUnitUtilityService', require('./services/org-unit-utility'));
    module.service('OrgUnitViewDataService', require('./services/org-unit-view-data'));
    module.service('OrgUnitViewOptionsService', require('./services/org-unit-view-options'));
};
