import {derived, writable} from "svelte/store";
import {dynamicSections, dynamicSectionsByKind} from "./dynamic-section-definitions";
import {sidebarVisible} from "../navbar/sidebar-store";
import _ from "lodash";
import {parseParams} from "../common/browser-utils";

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

    const params = parseParams(window.location.href);
    const sectionsStr = params.sections;

    return _.isEmpty(sectionsStr)
        ? []
        : sectionsStr
            .split(";")
            .map(s => Number(s))
            .map(id => sectionsById[id])
            .filter(d => ! _.isNil(d));
}


function getSectionsFromLocalStorage(pk) {
    const localStorageVal = window.localStorage.getItem(mkLocalStorageKey(pk)) || "[]";
    const sectionIds = JSON.parse(localStorageVal);
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

const baseActiveSectionState = {
    pageKind: null,
    sections: [],
    previous: [],
}

function createActiveSectionStore() {
    const {subscribe, set, update} = writable(baseActiveSectionState);

    const clear = () => update(d => {
        return {
            pageKind: d.pageKind,
            sections: [],
            previous: d.sections
        };
    });

    const add = (section) => update(d => {
        const cleanedActiveSections = removeSectionFromList(d.sections, section);
        return {
            pageKind: d.pageKind,
            sections: [section, ...cleanedActiveSections],
            previous: d.sections
        };

    });

    const remove = section => update(d => {
        return {
            pageKind: d.pageKind,
            sections: removeSectionFromList(d.sections, section),
            previous: d.sections
        };
    });

    const setPageKind = pageKind => update(c => {
        if (_.isEmpty(pageKind)) return;
        const localStorageSections = getSectionsFromLocalStorage(pageKind);
        const urlSections = getSectionsFromURL();
        const sectionsToUse = _.isEmpty(urlSections)
            ? localStorageSections
            : urlSections;
        return {
            pageKind,
            sections: sectionsToUse,
            previous: []
        };
    });

    const exitPage = () => {
        const url = window.location.origin + window.location.pathname;
        window.history.pushState({path: url}, "", url);
    };

    return {
        clear,
        add,
        remove,
        setPageKind,
        exitPage,
        subscribe,
    };
}

export const activeSections = createActiveSectionStore();

export const availableSections = derived(activeSections, d => {
    const sections = dynamicSectionsByKind[d.pageKind] || [];
    sidebarVisible.set(! _.isEmpty(sections));
    return sortSections(sections);
});


derived(
    activeSections,
    d => {
        if (_.isEmpty(d.pageKind)) {
            return;
        } else {
            const top3SectionIds = _
                .chain(d.sections)
                .compact()
                .take(3)
                .map(s => s.id)
                .value();

            window.localStorage.setItem(mkLocalStorageKey(d.pageKind), JSON.stringify(top3SectionIds));
            window.localStorage.setItem("active", JSON.stringify(d))
        }
    })
    .subscribe(() => {});

