import {truncateMiddle} from "../../common/string-utils";

function filter() {
    return (input = '',
            maxLength = 16,
            separator = ' ... ') => truncateMiddle(input, maxLength, separator);
}

export default filter;