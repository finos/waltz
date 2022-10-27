<script>
    import _ from "lodash";
    import {onMount} from "svelte";

    import NoData from "../../../common/svelte/NoData.svelte";
    import {entityNamedNoteTypeStore} from "../../../svelte-stores/entity-named-note-type-store";
    import {entityNamedNoteStore} from "../../../svelte-stores/entity-named-note-store";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityNamedNoteRemovalPanel from "./EntityNamedNoteRemovalPanel.svelte";
    import Toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import EntityNamedNoteEditPanel from "./EntityNamedNoteEditPanel.svelte";
    import EntityNamedNoteAddPanel from "./EntityNamedNoteAddPanel.svelte";

    export let parentEntityRef;

    const Modes = {
        VIEW: "VIEW",
        UPDATE: "UPDATE",
        ADD: "ADD",
        REMOVE: "REMOVE"
    };

    let mode = Modes.VIEW;
    let noteTypesCall = null;
    let notesCall = null;
    let type = [];
    let notes = [];
    let notesWithTypes = [];
    let selectedNote = null;
    let selectedType = null;

    function load() {
        noteTypesCall = entityNamedNoteTypeStore.findForRefAndUser(parentEntityRef, true);
        notesCall = entityNamedNoteStore.findForEntityReference(parentEntityRef, true);
    }

    function clearSelected() {
        selectedNote = null;
        selectedType = null;
    }

    function removeNote(note) {
        entityNamedNoteStore
            .remove(note.entityReference, note.namedNoteTypeId)
            .then(() => {
                load();
                Toasts.success(`Removed note`);
                mode = Modes.VIEW;
                clearSelected();
            })
            .catch(e => displayError("Failed to remove note", e));
    }

    function updateNote(evt) {
        const updatedText = evt.detail;
        entityNamedNoteStore
            .save(selectedNote.entityReference, selectedNote.namedNoteTypeId, updatedText)
            .then(() => {
                load();
                Toasts.success(`Updated note`);
                mode = Modes.VIEW;
                clearSelected();
            })
            .catch(e => displayError("Failed to save note", e));
    }

    function createNote(evt) {
        const {noteTypeId, noteText} = evt.detail;
        entityNamedNoteStore
            .save(parentEntityRef, noteTypeId, noteText)
            .then(() => {
                load();
                Toasts.success(`Saved note`);
                mode = Modes.VIEW;
                clearSelected();
            })
            .catch(e => displayError("Failed to save note", e));
    }


    function onCancel(message) {
        mode = Modes.VIEW;
        clearSelected();
        Toasts.info(message);
    }

    function onCancelRemoval() {
       onCancel("Cancelled removal of note");
    }

    function onCancelAddition() {
        onCancel("Cancelled addition of note");
    }

    function onCancelUpdate() {
        onCancel("Cancelled update of note");
    }

    function onShowRemovalConfirmation(note) {
        mode = Modes.REMOVE;
        clearSelected();
        selectedNote = note;
    }

    function onShowUpdatePanel(nt) {
        mode = Modes.UPDATE;
        clearSelected();
        selectedNote = nt.note;
        selectedType = nt.type;
    }

    function onShowAddPanel(type) {
        mode = Modes.ADD;
        clearSelected();
        selectedType = type;
    }

    onMount(() => {
        load();
    });

    $: typesWithOperations = $noteTypesCall?.data || [];
    $: notes = $notesCall?.data || [];

    $: {
        const typesById = _
            .chain(typesWithOperations)
            .keyBy(d => d.entity.id)
            .value();

        notesWithTypes = _
            .chain(notes)
            .map(note => {
                const typeAndOps = typesById[note.namedNoteTypeId];
                return {
                    note,
                    type: typeAndOps?.entity,
                    operations: typeAndOps?.operations
                };
            })
            .orderBy(d => d.type?.name)
            .value();
    }

    $: availableNoteTypes = _
        .chain(typesWithOperations)
        .filter(t => _.includes(t.operations, "ADD"))
        .map(t => t.entity)
        .value();

</script>


{#if mode===Modes.REMOVE}
    <EntityNamedNoteRemovalPanel note={selectedNote}
                                 on:confirm={() => removeNote(selectedNote)}
                                 on:cancel={onCancelRemoval}/>
{/if}

{#if mode===Modes.ADD}
    <EntityNamedNoteAddPanel availableNoteTypes={availableNoteTypes}
                             on:create={createNote}
                             on:cancel={onCancelAddition}/>
{/if}

{#if mode===Modes.UPDATE}
    <EntityNamedNoteEditPanel note={selectedNote}
                              type={selectedType}
                              on:save={updateNote}
                              on:cancel={onCancelUpdate}/>
{/if}

{#if mode===Modes.VIEW}
    <table class="table table-condensed small">
        <colgroup>
            <col width="30%">
            <col width="70%">
        </colgroup>
        <thead>
        <tr>
            <th>Note Type</th>
            <th>Note Text</th>
        </tr>
        </thead>
        <tbody>
        {#each notesWithTypes as nt}
            <tr class="waltz-visibility-parent">
                <td>
                    <div class="note-title">
                        {nt.type?.name}
                    </div>
                    <div class="small text-muted">
                        {nt.type?.description}
                    </div>
                    <div class="small text-muted">
                        Last Modified: <LastEdited entity={nt.note}/>
                    </div>
                    {#if !_.isEmpty(nt.operations)}
                        <div class="actions waltz-visibility-child-30">
                            {#if _.includes(nt.operations, "UPDATE")}
                                <button class="btn-skinny action"
                                        on:click={() => onShowUpdatePanel(nt)}>
                                    <Icon name="edit"/>
                                    Edit
                                </button>
                            {/if}
                            {#if _.includes(nt.operations, "REMOVE")}
                                <button class="btn-skinny action"
                                        on:click={() => onShowRemovalConfirmation(nt.note)}>
                                    <Icon name="trash"/>
                                    Delete
                                </button>
                            {/if}
                        </div>
                    {/if}
                </td>
                <td>
                    <Markdown context={{ref: parentEntityRef}}
                              text={nt.note.noteText}/>
                </td>
            </tr>
            {:else}
            <tr>
                <td colspan="2">
                    <NoData>
                        <b>No Notes</b> have been added
                    </NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>

    {#if !_.isEmpty(availableNoteTypes)}
        Additional note types are available for use.

        <button style="display: block"
                class="btn btn-sm btn-default"
                on:click={() => onShowAddPanel(type)}>
            <Icon name="plus"/>
            Add a new note
        </button>
    {/if}
{/if}
