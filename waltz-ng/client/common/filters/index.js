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
    module.filter('toDisplayName', require('./display-name-filter'));
    module.filter('toIconName', require('./icon-name-filter'));
    module.filter('toDescription', require('./description-filter'));
    module.filter('toFixed', require('./fixed-filter'));
    module.filter('isEmpty', require('./is-empty'));
};
