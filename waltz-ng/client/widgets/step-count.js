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

export default () => ({
    restrict: 'E',
    replace: true,
    template: '<svg width="50" height="50">\n    <circle r="25" cx="25" cy="25" fill="{{colour}}"></circle>\n    <text text-anchor="middle" x="25" y="32" font-size="24">\n        {{count}}\n    </text>\n</svg>',
    scope: {
        count: '@',
        colour: '@'
    }
});
