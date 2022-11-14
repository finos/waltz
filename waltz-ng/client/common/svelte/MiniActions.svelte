<script>
    import Icon from "./Icon.svelte";

    export let actions = []; // { name: "", icon: "", handleAction: (ctx) => {}, description?: "" }
    export let ctx = null;
</script>


<ul class="list-inline">
    {#each actions as action}
        <li class="clickable"
            title="{action.description}"
            on:click,keydown={() => action.handleAction(ctx)}>
            <button class="btn btn-skinny">
                {#if action.icon}<Icon name={action.icon}/>{/if}
                {#if action.name}{action.name}{/if}
            </button>
        </li>
    {/each}
</ul>


<style>
    li + li::before {
        content: "|";
        margin-right: 0.67em;
    }

    ul {
        padding: 0;
        display: inline-block;
    }
</style>
