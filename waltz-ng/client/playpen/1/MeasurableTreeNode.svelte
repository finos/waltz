<script>

    import Icon from "../../common/svelte/Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";

    export let tree;
    export let depth = 0;
    export let expanded = false;
    export let onSelect = () => console.log("selecting")
    export let onDeselect = () => console.log("deselecting")

    let dispatch = createEventDispatcher();

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
        <button class="btn btn-skinny"
                on:click|stopPropagation={() => console.log({tree}) || onDeselect(tree)}>
            <Icon name="check-square-o"/>
            {tree.name}
        </button>
    {:else}
        <button class="btn btn-skinny"
                on:click|stopPropagation={() => console.log({tree}) || onSelect(tree)}>
            <Icon name="square-o"/>
            {tree.name}
        </button>
    {/if}

{/if}

{#if expanded || tree.isExpanded}
    {#if tree.children}
        <ul class:root={depth === 0}>
            {#each tree.children as child}
                <li>
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
