export default () => ({
    restrict: 'E',
    replace: true,
    transclude: true,
    template: '<div class="waltz-section-actions"> <ng-transclude></ng-transclude> </div>'
});

