<script>

    import Markdown from "./Markdown.svelte";
    import SavingPlaceholder from "./SavingPlaceholder.svelte";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";

    export let text;
    export let onSave = (text) => console.log("Text to save", {text});
    export let editable;

    let workingText;
    let savePromise

    const dispatch = createEventDispatcher();

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        SAVING: "SAVING"
    }

    let activeMode = Modes.VIEW;

    function editText() {
        workingText = text;
        activeMode = Modes.EDIT
    }

    function save() {
        activeMode = Modes.SAVING;

        savePromise = onSave(workingText);

        savePromise
            .then(() => activeMode = Modes.VIEW);
    }

</script>


{#if activeMode === Modes.VIEW}
    <Markdown {text}/>
    {#if editable}
        <button class="btn btn-skinny"
                on:click={editText}>
            <Icon name="pencil"/>
            Edit
        </button>
    {/if}
{:else if activeMode === Modes.EDIT}
    <textarea class="form-control"
              rows="5"
              bind:value={workingText}/>
    <button class="btn btn-skinny"
            on:click={save}>
        <Icon name="floppy-o"/>
        Save
    </button>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.VIEW}>
        <Icon name="times"/>
        Cancel
    </button>
{:else if activeMode === Modes.SAVING}
    <SavingPlaceholder/>
{/if}