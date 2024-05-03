<script>

    import Markdown from "./Markdown.svelte";
    import SavingPlaceholder from "./SavingPlaceholder.svelte";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";

    export let text;
    export let onSave = (text) => console.log("Text to save", {text});
    export let editable;
    export let label;
    export let mandatory = false;

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

<span class="waltz-visibility-parent">
    {#if activeMode === Modes.VIEW}
        {#if !_.isEmpty(label)}
            <label for="text">
                {label} {#if mandatory}*{/if}
            </label>
            {#if editable}
                <button class="btn btn-skinny waltz-visibility-child-50"
                        on:click={editText}>
                    <Icon name="pencil"/>
                    Edit
                </button>
            {/if}
        {/if}
        <div id="text"
             class:waltz-scroll-region-350={_.size(text) > 1000}>
            <Markdown {text}/>
        </div>
        {#if mandatory}
            <div class="help-block">
                A comment is mandatory
            </div>
        {/if}
        {#if _.isEmpty(label) && editable}
            <button class="btn btn-skinny waltz-visibility-child-50"
                    on:click={editText}>
                <Icon name="pencil"/>
                Edit
            </button>
        {/if}
    {:else if activeMode === Modes.EDIT}
        {#if !_.isEmpty(label)}
            <label for="text">
                {label} {#if mandatory}*{/if}
            </label>
        {/if}
        <textarea class="form-control"
                  rows="5"
                  required={mandatory}
                  bind:value={workingText}></textarea>
        {#if mandatory}
            <div class="help-block">
                A comment is mandatory
            </div>
        {/if}
        <button class="btn btn-skinny"
                disabled={mandatory && _.isEmpty(_.trim(workingText))}
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
</span>
