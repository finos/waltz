<script>
    import {createEventDispatcher} from 'svelte';
    import {symbol} from "d3-shape";
    import _ from "lodash";

    const dispatch = createEventDispatcher();

    export let color = "#ccc";
    export let symbolsByName = {};
    export let startSymbol;

    $: dispatch("select", selectedSymbol);

    let selectedSymbol = startSymbol;

</script>


<div style="alignment: center; padding-top: 1em; padding-bottom: 1em;">
    <div style="display: inline-block">
        Symbols:
        &nbsp;
        {#each _.keys(symbolsByName) as symbolName}
            {#if symbolName !== "DEFAULT"}
            <span style="padding-right: 2px" class="clickable">
                <svg width="10"
                     height="10"
                     on:click,keydown|preventDefault={() => selectedSymbol = symbolName}
                     viewBox="0 0 10 10">
                    <path fill={color}
                          stroke={color}
                          transform="translate(5 5)"
                          d={symbolsByName[symbolName]()}>
                    </path>
                </svg>
            </span>
            {/if}
        {/each}
    </div>
    &nbsp;
    <div class="text-muted" style="display: inline-block">
        Original:
        &nbsp;
        <svg width="10"
             height="10"
             viewBox="0 0 10 10">
            <path fill={color}
                  stroke={color}
                  transform="translate(5 5)"
                  d={symbolsByName[startSymbol]()}>
            </path>
        </svg>
    </div>
</div>


<style>

</style>