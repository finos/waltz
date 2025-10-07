<script>
    import {basisOffsetDefaultOptions} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-utils";

    export let value;
    export let customValue;

</script>

<label for="basis-offset">
    Basis Offset
</label>

<select id="basis-offset"
        class="form-control"
        bind:value={value}>

    {#each basisOffsetDefaultOptions as d}
        <option value={d.code}>
            {d.name}
        </option>
    {/each}
</select>

{#if value === "OTHER"}
    <input class="form-control"
           id="customBasisOffset"
           required={true}
           placeholder="Enter a custom basis offset"
           bind:value={customValue}/>
{/if}

<div class="help-block">
    <slot name="help"/>
</div>