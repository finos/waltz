<script>
    import EntityLabel from "../../../../../common/svelte/EntityLabel.svelte";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    export let cellData = {};

    let references = [];

    $: references = cellData?.backingEntityReferences || [];

    $: height = references.length * 20;

</script>


<svg class="content"
     width="100%"
     {height}
     style="background: white">
    {#if cellData}
        <foreignObject width="300"
                       height="100%">
            <ul>
                {#each references as ref}
                    <li>
                        <EntityLabel {ref}/>
                        <EntityLink {ref}>
                            <Icon name="hand-o-right"/>
                        </EntityLink>
                    </li>
                {/each}
            </ul>
        </foreignObject>
    {/if}
</svg>


<style>


    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 12px
    }

    li {
        padding-top: 0.1em;
    }

</style>
