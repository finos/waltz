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

import angular from 'angular';
import tagsTemplate from './tags-input-template.html';
import orgUnitTemplate from './org-unit-input-template.html';


function customFieldSetup(formlyConfig) {
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

customFieldSetup.$inject = ['formlyConfig'];



export default () => {
    const module = angular.module('waltz.formly', []);

    module
        .run(customFieldSetup);

    module
        .config([
            'formlyConfigProvider',
            (formlyConfigProvider) => {
                formlyConfigProvider.setType({
                    name: 'tags-input',
                    template: tagsTemplate
                });

                formlyConfigProvider.setType({
                    name: 'org-unit-input',
                    template: orgUnitTemplate
                });
            }
        ]);

    return module.name;
};
