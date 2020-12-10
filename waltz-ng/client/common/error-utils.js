
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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
 * If e is provided then the `e.data.message` attribute will be5
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


