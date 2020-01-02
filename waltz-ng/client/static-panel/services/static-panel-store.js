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
    const baseUrl = `${base}/static-panel`;

    const findByGroups = (groups = []) => {
        return $http
            .get(`${baseUrl}/group`, {params: {group: groups}})
            .then(r => r.data);
    };

    const findAll = () =>
        $http
            .get(baseUrl)
            .then(r => r.data);

    const findByGroup = (group) => findByGroups([group]);

    const save = (p) =>
        $http
            .post(baseUrl, p)
            .then(r => r.data);

    return {
        findAll,
        findByGroup,
        findByGroups,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];



export const serviceName = 'StaticPanelStore';


export const StaticPanelStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'findAll'
    },
    findByGroup: {
        serviceName,
        serviceFnName: 'findByGroup',
        description: 'findByGroup'
    },
    findByGroups: {
        serviceName,
        serviceFnName: 'findByGroups',
        description: 'findByGroups'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'save'
    }
};