
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

import {checkIsEntityRef} from '../../common/checks';


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/bookmarks`;

    const save = (bookmark) => $http.post(baseUrl, bookmark);

    const findByParent = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const remove = (id) => $http.delete(`${baseUrl}/${id}`);

    return {
        save,
        findByParent,
        remove
    };

}


store.$inject = ['$http', 'BaseApiUrl'];


const serviceName = 'BookmarkStore';


export const BookmarkStore_API = {
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'save a bookmark'
    },
    findByParent: {
        serviceName,
        serviceFnName: 'findByParent',
        description: 'find by parent entity reference'
    },
    remove: {
        serviceName,
        serviceFnName: 'remove',
        description: 'remove a bookmark'
    }
};


export default {
    serviceName,
    store
};

