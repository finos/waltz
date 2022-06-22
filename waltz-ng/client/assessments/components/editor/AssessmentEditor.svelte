<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import AssessmentEditorView from "./AssessmentEditorView.svelte";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import {writable} from "svelte/store";
    import _ from "lodash";
    import toasts from "../../../svelte-stores/toast-store";
    import AssessmentEditorRemovalConfirmation from "./AssessmentEditorRemovalConfirmation.svelte";

    export let assessment;
    export let permissions = [];

    export let doRemove;
    export let doSave;
    export let doLock;
    export let doUnlock;
    export let onClose;

    const Modes = {
        VIEW: Symbol("VIEW"),
        REMOVE: Symbol("REMOVE"),
        EDIT: Symbol("EDIT")
    };

    const form = writable(Object.assign({}, assessment.rating));

    let mode = Modes.VIEW;
    let actions = [];

    function onEdit() {
        $form = Object.assign({}, assessment.rating);
        mode = Modes.EDIT;
    }

    function onCancel() {
        mode = Modes.VIEW;
    }

    function onSave() {
        console.log("onSave", {f: $form, a: assessment})
        doSave(assessment.definition.id, $form.ratingId, $form.comment)
            .catch(e => toasts.error("Could not update assessment: " + e.error))
            .finally(onCancel);
    }

    function onRemove() {
        mode = Modes.REMOVE;
    }

    $: {
        const unlockAction = {
            name: "Unlock",
            icon: "unlock-alt",
            help: "Removes the lock from this assessment, allowing users with edit permissions to update/remove",
            handleAction: () => doUnlock(assessment.definition.id)
        };
        const lockAction = {
            name: "Lock",
            icon: "lock",
            help: "Removes the lock from this assessment, preventing users from making updates or removing",
            handleAction: () => doLock(assessment.definition.id)
        };
        const editAction = {
            name: "Edit",
            icon: "edit",
            help: "Update this rating",
            handleAction: onEdit
        };
        const removeAction = {
            name: "Remove",
            icon: "trash",
            help: "Removes this rating",
            handleAction: onRemove
        };
        const closeAction = {
            name: "Close",
            icon: "times",
            help: "Stop showing this rating",
            handleAction: onClose
        };
        const saveAction = {
            name: "Save",
            icon: "save",
            help: "Persist this rating",
            handleAction: onSave
        };
        const cancelAction = {
            name: "Cancel",
            icon: "times",
            handleAction: onCancel
        };

        const locked = _.get(assessment, ["rating", "isReadOnly"], false);
        const hasRating = assessment.rating !== null;

        const canEdit = (permissions.includes("UPDATE") || permissions.includes("ADD")) && !locked;
        const canRemove = permissions.includes("REMOVE") && hasRating && !locked;
        const canLock = permissions.includes("LOCK") && !locked && hasRating;
        const canUnlock = permissions.includes("LOCK") && locked && hasRating;


        actions = _.compact([
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
            mode === Modes.VIEW
                ? closeAction
                : null,
            mode === Modes.EDIT
                ? saveAction
                : null,
            mode === Modes.EDIT || mode === Modes.REMOVE
                ? cancelAction
                : null,
        ]);
    }

</script>

<SubSection>
    <div slot="header">
        {assessment.definition.name}
    </div>
    <div slot="content">
        {#if assessment.definition.isReadOnly}
            <p class="help-block">
                <span style="color: orange">
                    <Icon name="lock"
                          size="lg"/>
                </span>
                This rating is read only
            </p>
        {/if}
        <p class="help-block">
            <Markdown text={assessment.definition.description}/>
        </p>

        <br>

        {#if mode === Modes.EDIT}

            <form on:submit|preventDefault={onSave}>
                <div class="form-group">
                    <label for="rating-dropdown">
                        Rating
                    </label>
                    <select id="rating-dropdown"
                            class="form-control"
                            bind:value={$form.ratingId}>
                        {#each assessment.dropdownEntries as entry}
                            <option value={entry.id}>
                                {entry.name}
                            </option>
                        {/each}
                    </select>
                </div>

                <div class="form-group">
                    <label for="comment">
                        Comment
                    </label>
                    <textarea id="comment"
                              class="form-control"
                              rows="6"
                              placeholder="This comment supports markdown"
                              bind:value={$form.comment}></textarea>
                </div>
            </form>

        {:else if mode === Modes.VIEW}

            <AssessmentEditorView {assessment}/>

        {:else if mode === Modes.REMOVE }
            <AssessmentEditorRemovalConfirmation {assessment}
                                                 {onCancel}
                                                 doRemove={() => doRemove(assessment.definition.id)}/>
        {/if}


    </div>
    <div slot="controls">
        <div class="pull-right small">
            <MiniActions actions={actions}/>
        </div>
    </div>

</SubSection>