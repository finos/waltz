export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        entries: '='
    },
    template: require('./change-log-section.html')
});
