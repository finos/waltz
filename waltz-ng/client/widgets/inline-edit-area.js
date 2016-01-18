/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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
