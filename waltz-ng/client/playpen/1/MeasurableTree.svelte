<script>

    import EntityLink from "../../common/svelte/EntityLink.svelte";

    export let tree;
    export let depth = 0;

</script>

{#if !tree.hideNode}
    <EntityLink ref={tree}/>
{/if}
{#if tree.children}
<ul class:root={depth === 0}>
    {#each tree.children as child}
        <li>
            <svelte:self tree={child} depth={++depth}/>
        </li>
    {/each}
</ul>
{/if}


<style>
    ul.root {
        padding-left: 1em;
    }

    ul {
        list-style-type: disc;
    }
</style>
