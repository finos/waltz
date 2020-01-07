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


import {checkIsDynamicSection} from "../common/checks";

export function sectionToTemplate(section, renderMode="standard") {
    try {
        checkIsDynamicSection(section);
    } catch (e) {
        console.log("Skipping section", { section, e });
    }

    const tagName = "waltz-" + section.componentId;

    return `
        <waltz-dynamic-section parent-entity-ref="$ctrl.parentEntityRef"
                               class="waltz-dynamic-section ${tagName} waltz-dynamic-section-render-mode-${renderMode} waltz-dynamic-section-${section.id}"
                               section="$ctrl.section"
                               on-remove="$ctrl.onRemove">
            <${tagName} parent-entity-ref="$ctrl.parentEntityRef"
                        filters="$ctrl.filters">
            </${tagName}>
        </waltz-dynamic-section>
    `;
}


