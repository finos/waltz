/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

function formlySetup(formlyConfig) {
    formlyConfig.setType({
        name: 'number',
        template: `
            <div class="form-group"
                 ng-class="{ 'has-error': form[options.id].$invalid }">
                <label class='control-label'
                       for='{{ options.name }}'>
                    <span ng-bind="to.label"></span>
                    <span ng-if="to.required">*</span>
                </label>
                <input class="form-control" 
                       id="{{ options.id }}"
                       ng-model="model[options.key]" 
                       type="number"/>
            </div>`
    });

    formlyConfig.setType({
        name: 'html',
        template: `
            <div class="form-group"
                 ng-class="{ 'has-error': form[options.id].$invalid }">
                <label class='control-label'
                       for='{{ options.name }}'>
                    <span ng-bind="to.label"></span>
                    <span ng-if="to.required">*</span>
                </label>
                <textarea style="font-family:Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New, monospace;"
                          class="form-control" 
                          id="{{ options.id }}"
                          ng-model="model[options.key]"
                          rows="10"/>
            </div>`
    });
}

formlySetup.$inject = ['formlyConfig'];


function uiSelectSetup(uiSelectConfig) {
    uiSelectConfig.theme = 'bootstrap';
    uiSelectConfig.resetSearchInput = true;
    uiSelectConfig.appendToBody = true;
}

uiSelectSetup.$inject = ['uiSelectConfig'];


function authProviderSetup($authProvider, BaseUrl) {
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

authProviderSetup.$inject = [
    '$authProvider',
    'BaseUrl',
];


function setup(module) {
    module
        .config(uiSelectSetup)
        .config(authProviderSetup)
        .run(formlySetup);
}


export default (module) => setup(module);