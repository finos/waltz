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
import angular from 'angular';

const Modes = {
    SAVING: Symbol(),
    VIEWING: Symbol(),
    EDITING: Symbol(),
    ERROR: Symbol()
};

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        value: '=',
        save: '&'
    },
    template: require('./inline-edit-area.html'),
    link: (scope) => {
        scope.mode = Modes.VIEWING;
        scope.Modes = Modes;

        scope.edit = () => {
            scope.editor = {
                modifiableValue: angular.copy(scope.value)
            };
            scope.mode = Modes.EDITING;
        };

        scope.cancel = () => {
            scope.mode = Modes.VIEWING;
        };

        scope.mySave = () => {
            scope.mode = Modes.SAVING;
            const valueToSave = scope.editor.modifiableValue;
            scope.save()(valueToSave, scope.value)
                .then(() => {
                    scope.mode = Modes.VIEWING;
                    scope.value = scope.editor.modifiableValue;

                }, (e) => {
                    scope.mode = Modes.ERROR;
                    scope.editor.error = e;
                });
        };
    }
});
