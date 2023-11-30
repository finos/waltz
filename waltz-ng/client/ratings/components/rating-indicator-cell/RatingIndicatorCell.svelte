<script>
    export let color;
    export let name;
    export let ratingGroup = null;
    export let description;
    export let showName = true;
    export let showGroup = false;

</script>


<span title={description}>
    <span class="cell"
          style={`background-color: ${color};`}>
    </span>
    {#if showGroup && ratingGroup}
        <span class="group text-muted">
            {ratingGroup} /
        </span>
    {/if}
    {#if showName}
        <span class="name">
            {name}
        </span>
    {/if}
</span>


<style>
    .cell {
        display: inline-block;
        width: 1.1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }

    .name {
        position: relative;
    }

    .group {
        position: relative;
    }
</style>