export default () => ({
    replace: true,
    restrict: 'E',
    scope: {
        person: '='
    },
    template: '<a ui-sref="main.person.view ({empId: person.employeeId})"> {{person.displayName}} </a>'
});
