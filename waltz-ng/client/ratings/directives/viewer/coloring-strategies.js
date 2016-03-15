import { perhaps } from  '../../../common';


function drill(appId, capId, ratingMap) {
    if (! ratingMap) { return []; }
    return perhaps(() => ratingMap[appId][capId], []);

}

function drillForRags(appId, capId, ratingMap) {

    return perhaps(() => {
        const ratings = drill(appId, capId, ratingMap);
        const rags =  _.chain(ratings)
            .values()
            .map(xs => xs[0])
            .compact()
            .map('ragRating')
            .uniq()
            .value();
        return rags;
    }, []);

}

function selectWorstFn(appId, capId, ratingMap) {
    const rags = drillForRags(appId, capId, ratingMap);

    if (_.contains(rags, 'R')) return 'R';
    if (_.contains(rags, 'A')) return 'A';
    if (_.contains(rags, 'G')) return 'G';
    return '';
}


function selectBestFn(appId, capId, ratingMap) {
    const rags = drillForRags(appId, capId, ratingMap);

    if (_.contains(rags, 'G')) return 'G';
    if (_.contains(rags, 'A')) return 'A';
    if (_.contains(rags, 'R')) return 'R';
    return '';
}


/**
 * Constructs an anonymous function which will drill into a ratingMap
 * (where a rating map is [appId -> capId -> measure -> [rating]])
 * to retrieve a rating (or '' if not found)
 *
 * @param code
 * @returns {Function}
 */
function selectByMeasureFn(measureCode) {

    return (appId, capId, ratingMap) => {
        return perhaps(() => drill(appId, capId, ratingMap)[measureCode][0].ragRating, '');
    };
}


// ---


export function mkSelectByMeasure(measure) {
    return {
        name: measure.name,
        description: `Focus on [${measure.description}] ratings`,
        fn: selectByMeasureFn(measure.code)
    }
}


export const selectBest = {
    name: 'Best',
    description: 'Colour by the \'best\' (G -> A -> R) of the ratings across all measures',
    fn: selectBestFn
};


export const selectWorst = {
    name: 'Worst',
    description: 'Colour by the \'worst\' (R -> A -> G) of the ratings across all measures',
    fn: selectWorstFn
};