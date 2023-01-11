import _ from "lodash";

export const Modes = {
    VIEW: "VIEW",
    REMOVE: "REMOVE",
    EDIT: "EDIT"
};


export function PermissionActions(doLock, doUnlock, onEdit, onRemove, onSave, onCancel, permissions, assessment) {
    this.doLock = doLock;
    this.doUnlock = doUnlock;
    this.onEdit = onEdit;
    this.onRemove = onRemove;
    this.onSave = onSave;
    this.onCancel = onCancel;
    this.permissions = permissions,
    this.assessment = assessment,
    this.determineActions = function(mode, rating) {

        if (_.isEmpty(this.permissions) || _.isEmpty(rating)) {
            return [];
        }

        const permissionsById = _.keyBy(this.permissions, d => d.ratingId);
        const defaultPermissions = _.find(this.permissions,d => d.isDefault);

        const permissionsForRating = _.get(permissionsById, rating.id, defaultPermissions);

        console.log("Hiii", {perms: this.permissions, permissionsById, rating, defaultPermissions, permissionsForRating});

        const unlockAction = {
            name: "Unlock",
            icon: "unlock-alt",
            help: "Removes the lock from this assessment, allowing users with edit permissions to update/remove",
            handleAction: () => this.doUnlock(this.assessment.definition.id, rating.id)
        };
        const lockAction = {
            name: "Lock",
            icon: "lock",
            help: "Removes the lock from this assessment, preventing users from making updates or removing",
            handleAction: () => this.doLock(this.assessment.definition.id, rating.id)
        };
        const editAction = {
            name: "Edit",
            icon: "edit",
            help: "Update this rating",
            handleAction: this.onEdit
        };
        const removeAction = {
            name: "Remove",
            icon: "trash",
            help: "Removes this rating",
            handleAction: this.onRemove
        };
        const saveAction = {
            name: "Save",
            icon: "save",
            help: "Persist this rating",
            handleAction: this.onSave
        };
        const cancelAction = {
            name: "Cancel",
            icon: "times",
            handleAction: this.onCancel
        };

        const locked = _.get(assessment, ["rating", "isReadOnly"], false);
        const hasRating = assessment.rating !== null;

        const canEdit = (_.includes(permissionsForRating.operations, "UPDATE") || _.includes(permissionsForRating.operations, "ADD")) && !locked;
        const canRemove = _.includes(permissionsForRating.operations, "REMOVE") && hasRating && !locked;
        const canLock = _.includes(permissionsForRating.operations, "LOCK") && !locked && hasRating;
        const canUnlock = _.includes(permissionsForRating.operations, "LOCK") && locked && hasRating;

        console.log({canEdit, canRemove, canLock, canUnlock, mode});

        const actions = _.compact([
            mode === Modes.VIEW && canLock
                ? lockAction
                : null,
            mode === Modes.VIEW && canUnlock
                ? unlockAction
                : null,
            mode === Modes.VIEW && canEdit
                ? editAction
                : null,
            mode === Modes.VIEW && canRemove
                ? removeAction
                : null,
            mode === Modes.EDIT
                ? saveAction
                : null,
            mode === Modes.EDIT || mode === Modes.REMOVE
                ? cancelAction
                : null,
        ]);


        console.log({actions});

        return actions;

    };
}
