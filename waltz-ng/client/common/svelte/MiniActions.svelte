<script>
    import Icon from "./Icon.svelte";

    export let actions = []; // { name: "", icon: "", handleAction: (ctx) => {}, description?: "" }
    export let ctx = null;
</script>


<ul class="list-inline">
    {#each actions as action}
        <li class="clickable"
            title="{action.description}"
            on:click={() => action.handleAction(ctx)}>
            <button class="btn-link">
                <Icon name={action.icon}/>&nbsp;{action.name}
            </button>
        </li>
    {/each}
</ul>


<style>
    li + li::before {
        content: "|";
    }

    ul {
        padding: 0;
        margin: 0;
    }
</style>
