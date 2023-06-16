import {derived, writable} from "svelte/store";
import _ from "lodash";
import {bulkLoadResolutionStatus} from "../../../common/services/enums/bulk-load-resolution-status";


export const UploadModes = {
    ADD_ONLY: "ADD_ONLY",
    REPLACE: "REPLACE"
}

export const rawInvolvements = writable("");
export const resolvedRows = writable([]);
export const uploadMode = writable(UploadModes.ADD_ONLY);

export const newInvolvements = derived(
    [resolvedRows],
    ([$resolvedRows]) => _.filter($resolvedRows, d => d.status === bulkLoadResolutionStatus.NEW.key));

export const existingInvolvements = derived(
    [resolvedRows],
    ([$resolvedRows]) => _.filter($resolvedRows, d => d.status === bulkLoadResolutionStatus.EXISTING.key));

export const resolutionErrors = derived(
    [resolvedRows],
    ([$resolvedRows]) => _.filter($resolvedRows, d => d.status === bulkLoadResolutionStatus.ERROR.key));

export const involvements = derived(
    [newInvolvements, existingInvolvements],
    ([$newInvolvements, $existingInvolvements]) => _.concat($newInvolvements, $existingInvolvements));
