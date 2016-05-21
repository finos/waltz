import {aggregatePeopleInvolvements} from "../involvement/involvement-utils";


const initialState = {
    bookmarks: [],
    changeInitiative: {},
    involvements: [],
    sourceDataRatings: []
};


function controller($q,
                    $stateParams,
                    bookmarkStore,
                    changeInitiativeStore,
                    involvementStore,
                    sourceDataRatingStore) {

    const { id } = $stateParams;
    const vm = Object.assign(this, initialState);


    changeInitiativeStore
        .getById(id)
        .then(ci => vm.changeInitiative = ci);


    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);


    bookmarkStore
        .findByParent({ kind: 'CHANGE_INITIATIVE', id })
        .then(bs => vm.bookmarks = bs);


    const involvementPromises = [
        involvementStore.findByEntityReference('CHANGE_INITIATIVE', id),
        involvementStore.findPeopleByEntityReference('CHANGE_INITIATIVE', id)
    ];
    $q.all(involvementPromises)
        .then(([relations, people]) => aggregatePeopleInvolvements(relations, people))
        .then(involvements => vm.involvements = involvements);
}


controller.$inject = [
    '$q',
    '$stateParams',
    'BookmarkStore',
    'ChangeInitiativeStore',
    'InvolvementStore',
    'SourceDataRatingStore'
];


const page = {
    template: require('./change-initiative-view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default page;

