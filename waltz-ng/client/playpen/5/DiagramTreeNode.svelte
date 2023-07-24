<script>

    import Icon from "../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let tree;
    export let depth = 0;
    export let expanded = false;
    export let onSelect = () => console.log("selecting")
    export let onDeselect = () => console.log("deselecting")

    function toggleExpanded() {
        expanded = !expanded;
    }

    function toggleSelected(tree) {
        if (tree.isSelected) {
            return onDeselect(tree);
        } else {
            return onSelect(tree);
        }
    }

</script>

{#if !tree.hideNode}
    {#if !_.isEmpty(tree.children) || !_.isEmpty(tree.items)}
        <button class="btn btn-skinny"
                on:click={toggleExpanded}>
            <Icon size="lg"
                  name={expanded ? "caret-down" : "caret-right"}/>
        </button>
    {/if}
    <button class="btn-skinny"
            on:click|stopPropagation={() => toggleSelected(tree)}>
        {tree.title}
    </button>
{/if}

{#if expanded || tree.isExpanded}
    {#if tree.children}
        <ul class:root={depth === 0}>
            {#each _.sortBy(tree.children, [d => d.position, d => d.title]) as child}
                <li style="white-space: nowrap">
                    <svelte:self tree={child}
                                 depth={++depth}
                                 {onSelect}
                                 {onDeselect}/>
                </li>
            {/each}
        </ul>
    {/if}
    {#if tree.items}
        <ul class:root={depth === 0}>
            {#each _.sortBy(tree.items, [d => d.position, d => d.title]) as item}
                <li>
                    <button class="btn-skinny"
                            on:click|stopPropagation={() => toggleSelected(item)}>
                        {item.title}
                    </button>
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
