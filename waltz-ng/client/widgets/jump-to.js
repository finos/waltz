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

import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import {dynamicSectionNavigationDefaultOffset} from "../dynamic-section/components/dynamic-section-navigation/dynamic-section-navigation";
import * as _ from "lodash";

/**
 * Additional scroll amount to compensate for navbar etc.
*/
const OFFSET = 160;


/**
 * An attribute directive which allows
 * for create in page scroll to anchors.
 * Usage:  &lt;div waltz-jump-to='some-id'>&lt;/div>
 * @param $window
 * @param dynamicSectionManager
 * @returns directive
 */
const directive = function($window,
                           dynamicSectionManager) {
    return {
        restrict: 'A',
        link: (scope, elem, attrs) => {
            // NOTE:  if you change the name of the directive
            // then the attr name will also change
            const target = attrs.waltzJumpTo;
            elem.on('click', () => {
                const section = _.find(dynamicSections, section => section.componentId === target);
                if(section != null) {
                    scope.$apply(() => (dynamicSectionManager.activate(section)));
                    $window.scrollTo(0, dynamicSectionNavigationDefaultOffset);
                } else {
                    console.log("waltz-jump-to section is null")
                }
            });
        }
    }
};


directive.$inject=[
    '$window',
    'DynamicSectionManager'
];


export default directive;