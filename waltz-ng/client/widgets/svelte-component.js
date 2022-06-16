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

import _ from "lodash";

const directive = function() {
    return {
        restrict: "E",
        link: (scope, elem, attrs) => {
            const props = _
                .chain(attrs)
                .map((p, k) => ({p, k}))
                .reject(d => d.k.startsWith("$"))
                .map(d => Object.assign(d, {v: _.get(scope, d.p, d.p)})) // fall back to d.p, as may be a literal value
                .reduce(
                    (acc, d) => {
                        acc[d.k] = d.v;
                        return acc;
                    },
                    {})
                .value();

            new props.component({
                target: elem[0],
                props
            });
        }
    };
};


directive.$inject=[];


export default directive;