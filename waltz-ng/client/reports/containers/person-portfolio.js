

class Controller {

    constructor($ngRedux, $scope, personPortfolioActions) {

        const onUpdate = (selectedState, actions) => {
            this.componentWillReceiveStateAndActions(selectedState, actions);
            Object.assign(this, selectedState, actions);
        };

        const unsubscribe = $ngRedux.connect(this.mapStateToThis, personPortfolioActions)(onUpdate);
        $scope.$on('$destroy', unsubscribe);

        this.fetchPersonPortfolioReportConfig(10);

    }

    componentWillReceiveStateAndActions(nextState, nextActions) {
    }


    // Which part of the Redux global state does our component want to receive?
    mapStateToThis(state) {

        return {
            people: state.people,
            appsByEmpId: state.personPortfolio,
            reportConfig: state.personPortfolioReportConfig[10]
        };
    }
}

Controller.$inject = ['$ngRedux', '$scope', 'PersonPortfolioActions'];


export default function directive() {
    return {
        restrict: 'E',
        controllerAs: 'ctrl',
        controller: Controller,
        template: require('./person-portfolio.html'),
        scope: {}
    };
}
