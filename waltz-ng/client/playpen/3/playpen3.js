import angular from 'angular';

const initData = {
    stick: false
};


function controller($anchorScroll,
                    $location,
                    $scope,
                    $window) {

    const vm = Object.assign(this, initData);

    angular
        .element($window)
        .bind("scroll", () => {
            vm.stick = $window.pageYOffset > 80;
            $scope.$apply()
        });


    vm.goTo = (sectionId) => {
        $location.hash(sectionId);
        $anchorScroll();
    };


}


controller.$inject = [
    '$anchorScroll',
    '$location',
    '$scope',
    '$window'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;