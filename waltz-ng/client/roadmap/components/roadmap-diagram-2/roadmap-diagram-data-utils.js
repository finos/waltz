import _ from "lodash";


export function mkRandomNode() {
    const t = _.random(0, 10000000);
    const rs = ["R", "A", "G", "Z", "X"];
    const mkRating = () => rs[_.random(0, rs.length)]
    const cell = {
        id: t,
        node: {name: `App ${t}`, externalId: `${t}-1`, description: "about test app"},
        change: {current: {rating: mkRating()}, future: {rating: mkRating()}},
        changeInitiative: t % 2
            ? {name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
            : null
    };
    return cell;
}


export function mkRandomNodes() {
    const howMany = _.random(1, 16);
    return _.map(_.range(0, howMany), () => mkRandomNode());
}