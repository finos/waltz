/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Additional scroll amount to compensate for navbar etc.
*/
const OFFSET = 160;


/**
 * An attribute directive which allows
 * for create in page scroll to anchors.
 * Usage:  &lt;div waltz-jump-to='some-id'>&lt;/div>
 * @param $location
 * @param $anchorScroll
 * @returns directive
 */
const directive = function($anchorScroll,
                           $location) {
    return {
        restrict: 'A',
        link: (scope, elem, attrs) => {
            // NOTE:  if you change the name of the directive
            // then the attr name will also change
            const target = attrs.waltzJumpTo;
            elem.on('click', () => {
                $anchorScroll.yOffset = OFFSET;
                $location.hash(target);
                $anchorScroll();
                scope.$apply();
            });
        }
    }
};


directive.$inject=[
    '$anchorScroll',
    '$location'
];


export default directive;