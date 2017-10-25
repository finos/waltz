import { truncate } from "../../common/string-utils";

function filter() {
    return (input = '',
            maxLength = 16,
            end = '...') => truncate(input, maxLength, end);
}

export default filter;