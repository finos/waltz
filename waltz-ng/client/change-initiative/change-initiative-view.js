import {aggregatePeopleInvolvements} from "../involvement/involvement-utils";

const initialState = {
    bookmarks: [],
    changeInitiative: {},
    involvements: [],
    sourceDataRatings: [],
    related: {
        appGroups: []
    }
};


function controller($q,
                    $stateParams,
                    appGroupStore,
                    bookmarkStore,
                    changeInitiativeStore,
                    involvementStore,
                    sourceDataRatingStore) {

    const { id } = $stateParams;
    const vm = Object.assign(this, initialState);

    const loadRelatedEntities = (id, rels = []) => {

        const relatedByKind = _.chain(rels)
            .flatMap(rel => ([rel.a, rel.b]))
            .reject(ref => ref.kind === 'CHANGE_INITIATIVE' && ref.id === id)
            .groupBy('kind', 'id')
            .mapValues(vs => _.map(vs, 'id'))
            .value();

        const promises = [
            appGroupStore.findByIds(relatedByKind.APP_GROUP)
        ];

        return $q
            .all(promises)
            .then(([appGroups]) => {
                console.log(appGroups)
                return { appGroups }
            });

    };


    changeInitiativeStore
        .getById(id)
        .then(ci => vm.changeInitiative = ci);


    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);


    bookmarkStore
        .findByParent({ kind: 'CHANGE_INITIATIVE', id })
        .then(bs => vm.bookmarks = bs);


    changeInitiativeStore
        .findRelatedForId(id)
        .then(rels => loadRelatedEntities(id, rels))
        .then(related => vm.related = related);


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
    'AppGroupStore',
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

