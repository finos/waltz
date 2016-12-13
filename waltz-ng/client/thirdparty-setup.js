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

function setup(module) {

    module.config([
        'uiSelectConfig',
        (uiSelectConfig) => {
            uiSelectConfig.theme = 'bootstrap';
            uiSelectConfig.resetSearchInput = true;
            uiSelectConfig.appendToBody = true;
        }
    ]);

    module.config([
        '$authProvider',
        'BaseUrl',
        function($authProvider, BaseUrl) {
            $authProvider.baseUrl = BaseUrl;
            $authProvider.withCredentials = false;

            $authProvider.google({
                clientId: 'Google account'
            });

            $authProvider.github({
                clientId: 'GitHub Client ID'
            });

            $authProvider.linkedin({
                clientId: 'LinkedIn Client ID'
            });

        }
    ]);

}


export default (module) => setup(module);