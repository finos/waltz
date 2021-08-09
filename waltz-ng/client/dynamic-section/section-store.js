import {derived, writable} from "svelte/store";
import {dynamicSections, dynamicSectionsByKind} from "./dynamic-section-definitions";
import {sidebarVisible} from "../navbar/sidebar-store";
import _ from "lodash";



/* Sort list of sections, ensuring 'change log' is the last one */
function sortList(list) {
    return _.orderBy(
        list,
        s => s === dynamicSections.changeLogSection
            ? "zzz"
            : _.toLower(s.name));
}


export const pageKind = writable("ORG_UNIT");

const localStorageKey = derived(pageKind, pk => `ls.waltz-user-section-ids-${pk}`);

function createActiveSectionStore() {
    const {subscribe, set, update} = writable([]);

    let currentKey = null;
    localStorageKey.subscribe(d => currentKey = d);

    pageKind.subscribe(d => set([]));



    return {
        clear: () => set([]),
        add: section => update(s => {
            const cleanedActiveSections = _.filter(s, d => d.id !== section.id);
            const updatedSections = [section, ...cleanedActiveSections];

            window.localStorage.setItem(
                currentKey,
                JSON.stringify(_
                    .chain(updatedSections)
                    .map(d => d.id)
                    .take(3)
                    .value()));
            //
            // // log component activations to access log
            // _.chain(toActivate)
            //     .map(s => `${$state.current.name}|${s.componentId}`)
            //     .forEach(state => accessLogStore.write(state, $stateParams))
            //     .value();


            return updatedSections;
        }),
        subscribe,
    };
}

export const activeSections = createActiveSectionStore();


function createAvailableSectionsStore() {
    return derived(pageKind, pk => {
        const sections = dynamicSectionsByKind[pk] || [];
        sidebarVisible.set(! _.isEmpty(sections));

        return sortList(sections);
    });
}

export const availableSections = createAvailableSectionsStore();