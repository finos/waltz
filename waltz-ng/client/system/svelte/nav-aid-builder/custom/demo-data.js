import {mkRef} from "../../../../common/entity-utils";

let boss = mkRef('PERSON', 3, "Big B. Boss");
let hilma = mkRef('PERSON', 4, "Hilma K");
let tracey = mkRef('PERSON', 25, "Tracey E");
let andy = mkRef('PERSON', 26, "Andy W");
let jackson = mkRef('PERSON', 27, "Jackson P");
let mary = mkRef('PERSON', 28, "Mary C");

export const demoData = {
    leaders: [
        {personId: 1, person: boss, title: "Chief Technology, Data & Innovation Officer"},
        {personId: 2, person: hilma, title: "Co Chief"}
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
        {unitId: 1, personId: 11, person: hilma, title: "Chief Information Officer"},
        {unitId: 2, personId: 12, person: tracey, title: "Chief Technology Officer"},
        {unitId: 3, personId: 13, person: mary, title: "Chief Risk Officer"},
        {unitId: 4, personId: 14, person: jackson, title: "Co Chief Information Officer - Private Bank"},
        {unitId: 4, personId: 15, person: andy, title: "Co Chief Information Officer - Private Bank"},
        {unitId: 5, personId: 16, person: jackson, title: "Co Chief Information Officer - Corporate Bank"},
        {unitId: 5, personId: 17, person: andy, title: "Co Chief Information Officer - Corporate Bank"}
    ]
};