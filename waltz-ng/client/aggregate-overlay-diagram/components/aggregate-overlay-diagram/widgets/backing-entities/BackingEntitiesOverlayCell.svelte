<script>

    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";

    export let cellData = {};

</script>


<svg class="content" viewBox="0 0 300 100">
    <foreignObject transform="translate(15, 5)"
                   width="270"
                   height="90">
        {#if cellData}
            <div class="waltz-scroll-region-100">
                <ul>
                    {#each cellData.backingEntityReferences as entity}
                        <li>
                            <EntityLink ref={entity}/>
                        </li>
                    {/each}
                </ul>
            </div>
        {:else}
            -
        {/if}
    </foreignObject>
</svg>


<style>


    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 16px
    }

    li {
        padding-top: 0.1em;
    }

</style>
