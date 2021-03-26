<script>
    export let scheme;
</script>

{#if scheme}
<ul class="list-inline">
    {#each scheme.ratings || [] as rating}
        <li>
            <div class="rating-square"
                 title={ rating.name }
                 style="background-color: {rating.color}">
            </div>
        </li>
    {/each}
</ul>
{/if}


<style>
    .rating-square {
        width: 1em;
        height: 1em;
        display: inline-block;
    }

    ul {
        margin: 0;
    }
</style>