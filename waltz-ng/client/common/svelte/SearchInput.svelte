<script>
    import Icon from "./Icon.svelte";

    export let value = "";
    export let placeholder = "Search";
</script>


<div class="input-group">
    <input class="waltz-search-control form-control"
           type="search"
           {placeholder}
           bind:value>
    <div class="input-group-addon">
        {#if value.length > 0}
            <span on:click,keydown={() => value = ""}
                  class="clickable">
                <Icon class="clickable"
                      name="times"/>
            </span>
        {:else}
            <Icon name="search"/>
        {/if}
    </div>
</div>