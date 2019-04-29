
const fallbackReasons = {
    404: "Not found",
    500: "Server error",
    501: "Disallowed"
};


function mkErrorMessage(message, e) {
    const fallbackReason = fallbackReasons[e.status] || "Unknown reason";

    const reason = e
        ? ": " + _.get(e, ["data", "message"], fallbackReason)
        : "";

    return `${message}${reason}`;
}

/**
 * Displays the given message as an error in the toaster.
 * If e is provided then the `e.data.message` attribute will be
 * included (if present).  Also prints the message to `console.log`
 *
 * @param notificationService
 * @param message
 * @param e
 */
export function displayError(notificationService, message, e) {
    const msg = mkErrorMessage(message, e);
    notificationService.error(msg);
    console.log(msg, e);
    return msg;
}


