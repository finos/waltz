import _ from "lodash";
import {randomPick} from "../../../../common";

const colorSchemes = [
    { fill: "red", stroke: "#ee9c9c" },
    { fill: "orange", stroke: "#e2a864" },
    { fill: "blue", stroke: "#74b7f1" },
    { fill: "pink", stroke: "#ecbbdf" },
    { fill: "green", stroke: "#97e580" },
    { fill: "purple", stroke: "#c7a5ea" }
];

const symbols = ["star", "square", "diamond", "circle", "wye", "triangle", "cross"];

function mkDecoratorId(symbol, fill, stroke) {
    return `${symbol}_${fill}_${stroke}`;
}

const symbolColorCombinations = _.flatMap(
    symbols,
    s => _.map(colorSchemes, (colorScheme) => Object.assign(
        {},
        colorScheme,
        {
            id: mkDecoratorId(s, colorScheme.fill, colorScheme.stroke),
            symbol: s
        })));


export function determineFillAndSymbol(existingGroups) {
    const taken = (desiredDecoration) => {
        const desiredDecorationId = mkDecoratorId(
            desiredDecoration.symbol,
            desiredDecoration.fill,
            desiredDecoration.stroke);

        return _.find(
            existingGroups,
            g => {
                const takenDecorationId = mkDecoratorId(g.group.symbol, g.group.fill, g.group.stroke);
                return desiredDecorationId === takenDecorationId;
            });
    };

    let candidate = randomPick(symbolColorCombinations);

    while (taken(candidate)) {
        candidate = randomPick(symbolColorCombinations);
    }

    return candidate;
}

