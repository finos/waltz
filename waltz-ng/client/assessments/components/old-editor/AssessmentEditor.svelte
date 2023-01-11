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
    import {PermissionActions, Modes} from "./assessment-editor-utils";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let assessment;
    export let permissions;

    export let doRemove;
    export let doSave;
    export let doLock;
    export let doUnlock;
    export let onClose;


    const form = writable(Object.assign({}, assessment.rating));

    let mode = Modes.VIEW;
    let actions = [];
    let dropdownConfig;
    let selectedRating

    function onEdit() {
        $form = Object.assign({}, assessment.rating);
        mode = Modes.EDIT;
    }


    $: permissionsByRatingId = _.keyBy($permissions, d => d.ratingId);
    $: defaultPermission = _.get($permissions, d => d.isDefault);

    let permissionActions;

    $: {

        if(doLock && doUnlock && onEdit && onRemove && onSave && onCancel) {
            console.log("create")
            permissionActions = new PermissionActions(doLock, doUnlock, onEdit, onRemove, onSave, onCancel, $permissions, assessment);
            console.log("done", {permissionActions});
        }
    }


    $: console.log({permissionActions, permissions: $permissions, assessment});

    function onCancel() {
        mode = Modes.VIEW;
    }

    function onSave() {
        console.log({form: $form});
        doSave(assessment.definition.id, $form.rating.id, $form.comment)
            .catch(e => toasts.error("Could not update assessment: " + e.error))
            .finally(onCancel);
    }

    function onRemove(ratingId) {
        mode = Modes.REMOVE;
        selectedRating = ratingId;
    }

    $: {
        const grouped = _
            .chain(assessment.dropdownEntries)
            .sortBy([d => d.ratingGroup, d => d.position, d => d.name])
            .groupBy("ratingGroup")
            .value();

        if (grouped[null] && _.size(grouped) > 1) {
            grouped["Ungrouped"] = grouped[null];
            delete grouped[null];
        }

        dropdownConfig = grouped[null]
            ? {style : "simple", options: grouped[null] }
            : {style : "grouped", groups: _.map(grouped, (v, k) => ({groupName: k, options: v}))};
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
        <p class="help-block">
            Cardinality: {assessment.definition.cardinality}
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
                            bind:value={$form.rating}>
                        {#if dropdownConfig.style === 'simple'}
                            {#each dropdownConfig.options as entry}
                                <option value={entry}>
                                    {entry.name}
                                </option>
                            {/each}
                        {:else if dropdownConfig.style === 'grouped'}
                            {#each dropdownConfig.groups as g}
                                <optgroup label={g.groupName}>
                                    {#each g.options as entry}
                                        <option value={entry}>
                                            {entry.name}
                                        </option>
                                    {/each}
                                </optgroup>
                            {/each}
                        {/if}
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


                <MiniActions actions={permissionActions.determineActions(mode, $form.rating)}/>
            </form>

        {:else if mode === Modes.VIEW}

            {#each assessment.ratings as rating}
                <AssessmentEditorView {rating}
                                      actions={permissionActions.determineActions(mode, rating)}/>
            {:else}
                <NoData>There are no ratings for this assessment</NoData>
            {/each}

            <div>
                <button class="btn btn-plain"
                        on:click={onClose}>
                    <Icon name="times"/>
                    Close
                </button>
                {#if assessment.definition.cardinality === "ZERO_MANY"}
                    <button class="btn btn-plain"
                            on:click={onClose}>
                        <Icon name="pencil"/>
                        Add
                    </button>
                {/if}
            </div>


        {:else if mode === Modes.REMOVE }
            <AssessmentEditorRemovalConfirmation {assessment}
                                                 {selectedRating}
                                                 {onCancel}
                                                 doRemove={() => doRemove(assessment.definition.id, selectedRating.id)}/>
        {/if}

    </div>

</SubSection>