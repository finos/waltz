<script>
    import {createEventDispatcher} from "svelte";

    export let value = [];   // tag list
    export let list = [];    // a list of tag suggestion

    let dirty = false;
    const dispatch = createEventDispatcher();

    let input = "";          // input value

    // pressed checks keyboard event for comma or Enter key.
    // If found, adds 'value' in the tag list.
    // REM: On the 'blur' event, if we exit the field with
    //      someting inside, consider it as a tag.
    function pressed(ev){
        // Check if conditions are met to do something.
        // If not, exit as early as possible.
        if(ev.type !== 'blur' && ev.key !== ',' && ev.key !== 'Enter') return;

        // Clean the remaining comma in input.
        input = input.replace(',','');

        if (input !== '') {
            // If we are here, we can add the tag to our list...
            // ... and clean the input for the next one.
            value = [...value, input];
            dirty = true;
            input = "";
        }

        if (ev.key === 'Enter' && ev.ctrlKey) {
            // If the uses is holding ctrl on when pressing enter
            onSave();
        }
    };

    // del deletes the 'idx' entry from the tag list.
    function del(idx){
        value.splice(idx,1);  // Remove the idx'th entry.
        value = value;        // Refresh for reactivity.
        dirty = true;
    }

    function onSave() {
        dispatch("save", value);
    }

    function onCancel() {
        dispatch("cancel");
    }

</script>
<main>
    <input list="tag_suggestion"
           title="Press enter or comma to start a new entry. Ctrl+Enter saves the list"
           on:blur={pressed}
           on:keyup={pressed}
           bind:value={input}/>

    <div class="tag-list">
        {#each value as t,i}
            <span class="tag">
                {t}&nbsp;<a href="#del" on:click={()=>del(i)}>⨉</a>
            </span>
        {/each}
    </div>
    <datalist id="tag_suggestion">
        {#each list as ts}
            <option>{ts}</option>
        {/each}
    </datalist>

    <div style="padding-top: 0.2em">
        <button class="btn-skinny"
                class:dirty
                on:click={onSave}>
            Save
        </button>
        <button class="btn-skinny"
                on:click={onCancel}>
            Cancel
        </button>
    </div>

</main>

<style>
    .tag-list {
        padding-top: 0.5em;
    }

    .dirty {
        font-weight: bolder;
    }
    .tag {
        display: inline-block;
        margin-right:0.33rem;
        padding: 0.2em;
        border-radius: 0.3em;
        background-color: #edf5fd;
        border: 1px solid #c0defa;
        margin-bottom: 0.2em;
    }
    .tag a {text-decoration: none; color: inherit;}
</style>