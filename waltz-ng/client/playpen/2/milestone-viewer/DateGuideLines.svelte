<script>
    import {dynamicDate, fixedDate} from "./stores/selected-dates";
    import {symbol, symbolCross} from "d3-shape";

    export let dateScale;
    export let width;
    export let height = 100;

    $: dateScale.range([0, width]);

    const cross = symbol().type(symbolCross).size(30)();

    function clearFixed() {
        fixedDate.set(null);
    }
    function mkFixed() {
        fixedDate.set($dynamicDate);
        dynamicDate.set(null);
    }
</script>


{#if $dynamicDate}
    <g class="fix"
       transform="translate({dateScale($dynamicDate?.getTime())} 0)">
        <path d={cross} on:click={() => mkFixed()}></path>
    </g>
    <line x1={dateScale($dynamicDate?.getTime())}
          x2={dateScale($dynamicDate?.getTime())}
          y1="10"
          y2={height - 45}
          class="dynamic"/>
{/if}

{#if $fixedDate}
    <g class="remove"
       transform="translate({dateScale($fixedDate?.getTime())} 0)">
        <path d={cross} on:click={() => clearFixed()}></path>
    </g>
    <line x1={dateScale($fixedDate?.getTime())}
          x2={dateScale($fixedDate?.getTime())}
          y1="10"
          y2={height - 45}
          class="fixed"/>
    <g class="remove"
       transform="translate({dateScale($fixedDate?.getTime())} {height - 30})">
        <path d={cross} on:click={() => clearFixed()}></path>
    </g>

{/if}

<style>
    .remove path {
        transform: rotate(45deg);
        fill: red;
    }
    .fix path {
        fill: green;
    }

    line.dynamic {
        stroke: #888;
        opacity: 0.05;
    }

    line.fixed {
        stroke: #ee3f3f;
        opacity: 0.05;
    }
</style>