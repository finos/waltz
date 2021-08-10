import {derived, writable} from "svelte/store";
import {dynamicSections, dynamicSectionsByKind} from "./dynamic-section-definitions";
import {sidebarVisible} from "../navbar/sidebar-store";
import _ from "lodash";

const sectionsById = _.keyBy(dynamicSections, d => d.id);

function removeSectionFromList(currentSections, section) {
    return _.filter(
        currentSections,
        d => d.id !== section.id);
}

/**
 * Sort list of sections, ensuring 'change log' is the last one
 */
function sortSections(list) {
    return _.orderBy(
        list,
        s => s === dynamicSections.changeLogSection
            ? "zzz"
            : _.toLower(s.name));
}


/**
 * Loads a list of sections from the 'section' url param.
 * This param is a semicolon delimited list of numbers
 * e.g.
 * `foo?sections=3;5;7`
 *
 * Sections are then resolved by id,
 * missing sections are removed from the result
 *
 * @returns  list of sections
 */
function getSectionsFromURL() {
    const params = (new URL(document.location)).searchParams;
    const sectionsStr = params.get("sections");
    return _.isEmpty(sectionsStr)
        ? []
        : sectionsStr
            .split(";")
            .map(s => Number(s))
            .map(id => sectionsById[id])
            .filter(d => ! _.isNil(d));
}


function getSectionsFromLocalStorage(pk) {
    const sectionIds = JSON.parse(window.localStorage.getItem(mkLocalStorageKey(pk))) || [];
    const sections = _
        .chain(sectionIds)
        .map(id => sectionsById[id])
        .compact()
        .value();
    return sections;
}


/**
 * constructs a key suitable for storing section ids in the browsers local storage
 * @param kind
 * @returns {string}
 */
function mkLocalStorageKey(kind) {
    return `ls.waltz-user-section-ids-${kind}`;
}


export const pageKind = writable("ORG_UNIT");

export const availableSections = derived(pageKind, pk => {
    const sections = dynamicSectionsByKind[pk] || [];
    sidebarVisible.set(! _.isEmpty(sections));
    return sortSections(sections);
});


function createActiveSectionStore() {
    const {subscribe, set, update} = writable([]);

    const clear = () => set([]);

    const add = section => update(currentSections => {
        const cleanedActiveSections = removeSectionFromList(currentSections, section);
        return [section, ...cleanedActiveSections];
    });

    const remove = section => update(currentSections => {
        return removeSectionFromList(currentSections, section);
    });

    pageKind.subscribe(pk => {
        const localStorageSections = getSectionsFromLocalStorage(pk);
        const urlSections = getSectionsFromURL();
        const sectionsToUse = _.isEmpty(urlSections)
            ? localStorageSections
            : urlSections;

        set(sectionsToUse);
    });

    return {
        clear,
        add,
        remove,
        subscribe,
    };
}

export const activeSections = createActiveSectionStore();

/**
 * listen to page kind and sections, if they change persist to local storage
 */
derived(
    [pageKind, activeSections],
    ([pk, sections]) => {
        window.localStorage.setItem(
            mkLocalStorageKey(pk),
            JSON.stringify(_
                .chain(sections)
                .map(d => d.id)
                .take(3)
                .value()));
    })
    .subscribe(() => {});

derived(
    activeSections,
    sections => {
        const top3SectionIds = _
            .chain(sections)
            .compact()
            .take(3)
            .map(s => s.id)
            .value();

        const paramValue = _.join(top3SectionIds, ";");

        const url = window.location.origin + window.location.pathname + "?sections=" + paramValue;
        window.history.pushState({path: url}, "", url);
    })
    .subscribe(() => {})