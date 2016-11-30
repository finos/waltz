import {toDomain} from "../../common";

function filter() {
    return (url) => toDomain(url);
}

export default filter;