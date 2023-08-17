import _ from "lodash";

export function prettyHTML(elemHtml) {
    if (_.isNil(elemHtml)) {
        return "";
    }
    const tab = "    ";
    let result = "";
    let indent= "";

    elemHtml
        .split(/>\s*</)
        .forEach(function(element) {
            if (element.match( /^\/\w/ )) {
                indent = indent.substring(tab.length);
            }

            result += indent + "<" + element + ">\r\n";

            if (element.match( /^<?\w[^>]*[^\/]$/ ) && !element.startsWith("input")  ) {
                indent += tab;
            }
        });

    return result.substring(1, result.length - 3);
}