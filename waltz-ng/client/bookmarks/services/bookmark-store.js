
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

export default store;
