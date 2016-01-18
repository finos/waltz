export default () => ({
    restrict: 'E',
    replace: true,
    template: '<table class="waltz-display-field-table"><tr><td class="waltz-display-field-label">{{label}}</td><td class="waltz-display-field-value"><ng-transclude></ng-transclude></td></tr>',
    scope: {
        label: '@'
    },
    transclude: true
});
