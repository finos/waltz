<script>
    export let color;
    export let name;
    export let description;
    export let showName = true;
</script>


<span title={description}>
    <span class="cell"
          style={`background-color: ${color};`}>
    </span>
    {#if showName}
        <span class="name">
            {name}
        </span>
    {/if}
</span>


<style>
    .cell {
        top: 4px;
        display: inline-block;
        width: 1em;
        height: 1em;
        border: 1px solid;
        border-radius: 2px;
    }

    .name {
        position: relative;
    }
</style>