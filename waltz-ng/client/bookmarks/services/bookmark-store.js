
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

