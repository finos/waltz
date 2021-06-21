import {blue, red} from "../../../common/colors"

export function determineStylingBasedUponLifecycle(status) {
    switch (status) {
        case "PENDING":
            return {color: blue, dashArray: "6 3"};
        case "REMOVED":
            return {color: red, dashArray: "3 6"};
        default:
            return {color: "#888888", dashArray: "0"};
    }
}