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
