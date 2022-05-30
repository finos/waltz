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
        display: inline-block;
        width: 1.3em;
        height: 1.3em;
        border: 1px solid;
        border-radius: 6px;
    }

    .name {
        position: relative;
        top: -4px;
    }
</style>