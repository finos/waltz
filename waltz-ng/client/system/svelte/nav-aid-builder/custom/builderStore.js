import {writable} from "svelte/store";
import {demoData} from "./demo-data";
import {personStore} from "../../../../svelte-stores/person-store";

function generateUUID() { // Public Domain/MIT
    let d = new Date().getTime();//Timestamp
    let d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now()*1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = Math.random() * 16;//random number between 0 and 16
        if(d > 0){//Use timestamp until depleted
            r = (d + r)%16 | 0;
            d = Math.floor(d/16);
        } else {//Use microseconds since page-load if supported
            r = (d2 + r)%16 | 0;
            d2 = Math.floor(d2/16);
        }
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

const initialModel = {
        leaders: [], // { person: ref, title: str }
        groups: [], // {groupId, name}
        units: [], // {unitId, name, groupId }
        people: []//  { personId, unitId, personRef }
};

function fixupPerson(d, prop) {
    if (!d[prop]) {
        d[prop] = generateUUID();
    }

    return d;
}

function createModelStore() {
    const {set, update, subscribe} = writable(demoData);

    return {
        subscribe,
        reset: () => set(initialModel),

        addLeader: (p) => update(m => _.set(m, "leaders", _.concat(m.leaders, [fixupPerson(p, "personId")]))),
        removeLeader: (personId) => update( m => _.set(m, "leaders", _.reject(m.leaders, p => p.personId === personId))),
        updateLeader: (newPerson) => update( m => _.set(m, "leaders", _.map(m.leaders, existing => existing.personId === newPerson.personId ? newPerson : existing))),

        addGroup: (name) => update(m => _.set(m, "groups", _.concat(m.groups, [{groupId: generateUUID(), name}]))),
        removeGroup: (groupId) => update( m => _.set(m, "groups", _.reject(m.groups, g => g.groupId === groupId))),
        updateGroup: (newGroup) => update(m => _.set(m, "groups", _.map(m.groups, existing => existing.groupId === newGroup.groupId ? newGroup : existing))),

        addUnit: (groupId, name) => update(m => _.set(m, "units", _.concat(m.units, [{groupId, unitId: generateUUID(), name}]))),
        removeUnit: (unitId) => update( m => _.set(m, "units", _.reject(m.units, u => u.unitId === unitId))),
        updateUnit: (newUnit) => update(m => _.set(m, "units", _.map(m.units, existing => existing.unitId === newUnit.unitId ? newUnit : existing))),

        addPerson: (p) => update(m => _.set(m, "people", _.concat(m.people, [fixupPerson(p, "personId")]))),
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


function createLikelyPeopleStore() {
    const {subscribe, set} = writable([])

    model.subscribe($model => {
        const personIds = _.map($model.leaders, d => d.person.id);
        let call = personStore.findDirectsForPersonIds(personIds);
        call.subscribe(d => set(d.data));
    });

    return {
        subscribe
    };
}

export const renderMode = createRenderModeStore();
export const model = createModelStore();
export const likelyPeople = createLikelyPeopleStore();

likelyPeople.subscribe(() => {});