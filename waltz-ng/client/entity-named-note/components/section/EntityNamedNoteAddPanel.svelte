<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import EntityNamedNoteEditPanel from "./EntityNamedNoteEditPanel.svelte";

    export let availableNoteTypes = [];

    const dispatch = createEventDispatcher();

    const Modes = {
        LIST: "LIST",
        EDIT: "EDIT"
    };

    let mode = Modes.LIST
    let emptyNote = null;
    let selectedType = null;

    function onShowEditPanel(type) {
        mode = Modes.EDIT;
        emptyNote = {noteText: ""};
        selectedType = type;
    }

    function onCancelCreate() {
        dispatch("cancel");
    }

    function createNote(evt) {
        const newEvt = {
            noteText: evt.detail,
            noteTypeId: selectedType.id
        };
        dispatch("create", newEvt);
    }

    function reset() {
        mode = Modes.LIST;
        selectedType = null;
    }

    onMount(reset);



</script>

{#if mode === Modes.EDIT}
    <EntityNamedNoteEditPanel note={emptyNote}
                              type={selectedType}
                              on:save={createNote}
                              on:cancel={onCancelCreate}/>
{/if}
&nbsp;
{#if mode === Modes.LIST}
    <div class="edit-box">
        <h4>
            <Icon name="plus"/>
            Add a new note:
        </h4>

        The following note types are available:
        <br>

        <dl>
            {#each availableNoteTypes as type}
                <dt>
                    <button class="btn-skinny"
                            on:click={() => onShowEditPanel(type)}>
                        <Icon name="sticky-note-o"/>
                        {type.name}
                    </button>
                </dt>
                <dd>
                    <div class="small text-muted">
                        {type.description}
                    </div>
                </dd>
            {/each}
        </dl>

        <button class="btn-skinny"
                on:click={() => dispatch("cancel")}>
            Cancel
        </button>

    </div>
{/if}


<style>

    dl {
        padding-top: 0.5em;
        padding-left: 1em;
    }

    dd {
        padding-bottom: 0.5em;
    }

    .edit-box{
        border-width: 1px;
        border-style: solid;
        border-color: #59a1f1;
        background-color: #e9f7fa;
        padding: 1.5em 2em;
        border-radius: 2px;
    }
</style>