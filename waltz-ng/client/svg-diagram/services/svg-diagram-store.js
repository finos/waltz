
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

export function store($http, base) {

    const findByGroups = (groups = []) =>
        $http
            .get(`${base}/svg-diagram/group`, { params : { group: groups } })
            .then(r => r.data);

    const findByGroup = (group) => findByGroups([group]);

    return {
        findByGroup,
        findByGroups
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'SvgDiagramStore';


export const SvgDiagramStore_API = {
    findByGroup: {
        serviceName,
        serviceFnName: 'findByGroup',
        description: 'executes findByGroup'
    },
    findByGroups: {
        serviceName,
        serviceFnName: 'findByGroups',
        description: 'executes findByGroups'
    }
};
