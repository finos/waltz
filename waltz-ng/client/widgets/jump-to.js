/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import {dynamicSectionNavigationDefaultOffset} from "../dynamic-section/components/dynamic-section-navigation/dynamic-section-navigation";
import * as _ from "lodash";


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
        restrict: "A",
        link: (scope, elem, attrs) => {
            // NOTE:  if you change the name of the directive
            // then the attr name will also change
            const target = attrs.waltzJumpTo;
            elem.on("click", () => {
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
    "$window",
    "DynamicSectionManager"
];


export default directive;