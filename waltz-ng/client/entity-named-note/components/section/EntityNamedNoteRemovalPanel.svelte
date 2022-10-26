<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import {createEventDispatcher} from "svelte";

    export let note = null;

    const dispatch = createEventDispatcher();

</script>

<div class="removal-box">
    <Icon name="trash"/>
    Are you sure you would like to remove this note?

    <div class="quoted-note">
        <Markdown text={note.noteText}/>
    </div>
    <br>
    <button class="btn btn-danger"
            on:click={() => dispatch("confirm")}>
        Yes
    </button>
    &nbsp;
    <button class="btn-skinny"
            on:click={() => dispatch("cancel")}>
        No
    </button>

</div>

<style>

    .quoted-note {
        border-style: solid;
        border-color: #f8868a;
        background-color: #fcf3f5;
        padding: 0.5em;
    }
    .removal-box{
        border-width: 1px;
        border-style: solid;
        border-color: #d93f44;
        background-color: #fae9ee;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        padding-top: 1.5em;
        border-radius: 2px;
    }
</style>