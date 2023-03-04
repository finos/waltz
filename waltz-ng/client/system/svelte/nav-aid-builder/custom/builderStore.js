import {writable} from "svelte/store";
import {demoData} from "./demo-data";


const initialModel = {
        leaders: [], // { person: ref, title: str }
        groups: [], // {groupId, name}
        units: [], // {unitId, name, groupId }
        people: []//  { personId, unitId, personRef }
};


function nextId() {
    return Math.random();
}


function createModelStore() {
    const {set, update, subscribe} = writable(demoData);

    return {
        subscribe,
        reset: () => set(initialModel),

        addLeader: (p) => update(m => _.set(m, "leaders", _.concat(m.leaders, [p]))),
        removeLeader: (personId) => update( m => _.set(m, "leaders", _.reject(m.leaders, p => p.personId === personId))),
        updateLeader: (newPerson) => update( m => _.set(m, "leaders", _.map(m.leaders, existing => existing.personId === newPerson.personId ? newPerson : existing))),

        addGroup: (name) => update(m => _.set(m, "groups", _.concat(m.groups, [{groupId: nextId(), name}]))),
        removeGroup: (groupId) => update( m => _.set(m, "groups", _.reject(m.groups, g => g.groupId === groupId))),
        updateGroup: (newGroup) => update(m => _.set(m, "groups", _.map(m.groups, existing => existing.groupId === newGroup.groupId ? newGroup : existing))),

        addUnit: (groupId, name) => update(m => _.set(m, "units", _.concat(m.units, [{groupId, unitId: nextId(), name}]))),
        removeUnit: (unitId) => update( m => _.set(m, "units", _.reject(m.units, u => u.unitId === unitId))),
        updateUnit: (newUnit) => update(m => _.set(m, "units", _.map(m.units, existing => existing.unitId === newUnit.unitId ? newUnit : existing))),

        removePerson: (personId) => update( m => _.set(m, "people", _.reject(m.people, p => p.personId === personId))),
        updatePerson: (newPerson) => update( m => _.set(m, "people", _.map(m.people, existing => existing.personId === newPerson.personId ? newPerson : existing))),
    };
}


export const RenderModes = {
    DEV: 'dev',
    LIVE: 'live'
};


function createRenderModeStore() {

    const {subscribe, update, set} = writable(RenderModes.DEV);

    return {
        subscribe,
        setLiveMode: () => set(RenderModes.LIVE),
        setDevMode: () => set(RenderModes.DEV),
        toggle: () => update(c => c === RenderModes.DEV
            ? RenderModes.LIVE
            : RenderModes.DEV)
    };

}


export const renderModeStore = createRenderModeStore();
export const model = createModelStore();