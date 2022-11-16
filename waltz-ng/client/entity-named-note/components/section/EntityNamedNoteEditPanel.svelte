<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import {createEventDispatcher} from "svelte";

    export let note = null;
    export let type = null;

    const dispatch = createEventDispatcher();

    let working = note.noteText

</script>

<div class="edit-box">
    <h4>
        <Icon name="edit"/>
        Editing: {type.name}
    </h4>

    <table class="table">
        <colgroup>
            <col width="50%">
            <col width="50%">
        </colgroup>
        <tr>
            <td>
                <textarea rows="8"
                          placeholder="Enter note text here.  Markdown formatting is supported."
                          bind:value={working}/>
            </td>
            <td style="vertical-align: top">
                <div class="preview">
                    <Markdown text={working || "*Preview will appear here*" }/>
                </div>

            </td>
        </tr>
    </table>
    <br>
    <button class="btn btn-success"
            on:click={() => dispatch("save", working)}>
        Save
    </button>
    &nbsp;
    <button class="btn-skinny"
            on:click={() => dispatch("cancel")}>
        Cancel
    </button>

</div>

<style>

    textarea {
        width: 100%;
        font-family: monospace;
    }

    .preview {
        border-style: solid;
        border-color: #d7d8da;
        background-color: #f3fbfc;
        padding: 0.5em;
        margin-left: 0.5em;
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