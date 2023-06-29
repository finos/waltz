<script>

    import Icon from "./Icon.svelte";
    import _ from "lodash";

    export let tree;
    export let depth = 0;
    export let expanded = false;
    export let onSelect = () => console.log("selecting")
    export let onDeselect = () => console.log("deselecting")

    function toggleExpanded() {
        expanded = !expanded;
    }

</script>

{#if !tree.hideNode}
    {#if !_.isEmpty(tree.children)}
        <button class="btn btn-skinny"
                on:click={toggleExpanded}>
            <Icon size="lg"
                  name={expanded ? "caret-down" : "caret-right"}/>
        </button>
    {/if}
    {#if tree.isSelected}
        <button class="btn-skinny"
                on:click|stopPropagation={() => onDeselect(tree)}>
            <Icon name="check-square-o"/>
            {tree.name}
        </button>
    {:else}
        <button class="btn-skinny"
                on:click|stopPropagation={() => onSelect(tree)}>
            <Icon name="square-o"/>
            {tree.name}
        </button>
    {/if}
{/if}

{#if expanded || tree.isExpanded}
    {#if tree.children}
        <ul class:root={depth === 0}>
            {#each _.sortBy(tree.children, [d => d.position, d => d.name]) as child}
                <li style="white-space: nowrap">
                    <svelte:self tree={child}
                                 depth={++depth}
                                 {onSelect}
                                 {onDeselect}/>
                </li>
            {/each}
        </ul>
    {/if}
{/if}


<style>
    ul.root {
        padding-left: 1em;
    }

    ul {
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>
