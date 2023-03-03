import {mkRef} from "../../../../common/entity-utils";

let boss = mkRef('PERSON', 23, "Big B. Boss");
let gil = mkRef('PERSON', 24, "Gil P");
let gordon = mkRef('PERSON', 25, "Gordon M");
let stefan = mkRef('PERSON', 26, "Stefan P");
let marcus = mkRef('PERSON', 27, "Marcus H");
let marie = mkRef('PERSON', 28, "Marie L");

export const demoData = {
    leaders: [
        {person: boss, title: "Chief Technology, Data & Innovation Officer"},
        {person: gil, title: "Co Chief"}
    ],
    groups: [
        {groupId: 1, name: "Functions"},
        {groupId: 2, name: "Chief Information Officers"}
    ],
    units: [
        {groupId: 1, unitId: 1, name: "Cloud and Innovation Network"},
        {groupId: 1, unitId: 2, name: "Chief Technology Office"},
        {groupId: 1, unitId: 3, name: "Chief Risk Office"},
        {groupId: 2, unitId: 4, name: "Private Bank"},
        {groupId: 2, unitId: 5, name: "Corporate Bank"}
    ],
    people: [
        {unitId: 1, personId: 1, person: gil, title: "Chief Information Officer"},
        {unitId: 2, personId: 2, person: gordon, title: "Chief Technology Officer"},
        {unitId: 3, personId: 3, person: marie, title: "Chief Risk Officer"},
        {unitId: 4, personId: 4, person: marcus, title: "Co Chief Information Officer - Private Bank"},
        {unitId: 4, personId: 5, person: stefan, title: "Co Chief Information Officer - Private Bank"},
        {unitId: 5, personId: 6, person: marcus, title: "Co Chief Information Officer - Corporate Bank"},
        {unitId: 5, personId: 7, person: stefan, title: "Co Chief Information Officer - Corporate Bank"}
    ]
};