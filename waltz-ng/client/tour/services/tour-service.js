import {introJs} from 'intro.js'
import _ from 'lodash';


const defaultCompletionCallback = () => console.log('tourService: completionCallback missing');


function initialiseIntroJs(tourSteps = [],
                           completionCallback = defaultCompletionCallback) {
    if (tourSteps.length < 0) return null;

    const introSteps = _.map(tourSteps, s => {
        return {
            element: s.selector,
            intro: s.description,
            position: s.position
        }
    });

    const introOptions = {
        steps: introSteps,
        showStepNumbers: false,
        showProgress: true,
        showBullets: false,
        exitOnOverlayClick: true,
        exitOnEsc: true,
        nextLabel: '<strong>Next</strong>',
        prevLabel: '<span>Previous</span>',
        skipLabel: 'Exit',
        doneLabel: 'Done',
        overlayOpacity: 0.6,
        disableInteraction: true
    };

    const intro = new introJs();
    intro.setOptions(introOptions);
    intro.onexit(completionCallback);
    intro.oncomplete(completionCallback);

    return intro;
}


function hasSeen(key,
                 preferences = {}) {
    const preferenceKey = mkPreferenceKey(key);
    const preference = preferences[preferenceKey];
    return preference
        ? preference.value === 'true'
        : false;
}


function mkPreferenceKey(key) {
    return `${key}.tour.seen`;
}


function markSeen(userPreferenceStore, key) {
    const preferenceKey = mkPreferenceKey(key);
    userPreferenceStore.savePreference(preferenceKey, 'true');
}


function service($q,
                 $timeout,
                 tourStore,
                 userPreferenceService) {

    /** Not async, returns tour-start function or null */
    const initialiseWithSteps = (steps = []) => {
        const intro = initialiseIntroJs(steps);
        return intro
            ? () => intro.start()
            : null;
    };

    /** Async, returns tour-start function or null */
    const initialiseForKey = (key, autoPlay = false) => {
        const promises = [
            tourStore.findForKey(key),
            userPreferenceService.loadPreferences()
        ];

        return $q
            .all(promises)
            .then(([steps = [], preferences = {}]) => {
                const intro = initialiseIntroJs(
                    steps,
                    () => markSeen(userPreferenceService, key));

                const seen = hasSeen(key, preferences);

                if (! seen && autoPlay && steps.length > 0) {
                    intro.start();
                };

                return {
                    seen,
                    start: intro
                        ? () => intro.start()
                        : () => {},
                    stepCount: steps.length
                };
            });
    };


    /**
     * You probably shouldn't be using this... ;)
     * @param tour
     * @returns {*}
     */
    const delayedStart = (tour) => {
        return $timeout(() => tour.start(), 1500);
    };


    return {
        initialiseWithSteps,
        initialiseForKey,
        delayedStart
    };
}


service.$inject = [
    '$q',
    '$timeout',
    'TourStore',
    'UserPreferenceService'
];


export default service;
