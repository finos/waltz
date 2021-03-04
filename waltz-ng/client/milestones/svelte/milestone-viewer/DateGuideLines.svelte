<script>
    import {dynamicDate, fixedDate} from "./stores/selected-dates";
    import {symbol, symbolCross} from "d3-shape";

    export let dateScale;
    export let width;
    export let height = 100;

    const today = new Date();
    const cross = symbol().type(symbolCross).size(30)();

    $: dateScale.range([0, width]);

    function clearFixed() {
        fixedDate.set(null);
    }

    function mkFixed() {
        fixedDate.set($dynamicDate);
        dynamicDate.set(null);
    }

    function selectToday() {
        $dynamicDate
            ? fixedDate.set(today)
            : dynamicDate.set(today);
    }

</script>


<g class="today"
   transform="translate({dateScale(today.getTime())} 10)">
    <path d={cross} />
    <circle class="hitbox"
            r="10"
            pointer-events="all"
            on:click={() => selectToday()}/>
</g>

<line x1={dateScale(today.getTime())}
      x2={dateScale(today.getTime())}
      y1="20"
      y2={height - 45}
      class="today"/>


{#if $dynamicDate}
    <g class="fix"
       transform="translate({dateScale($dynamicDate?.getTime())} 10)">
        <path d={cross} ></path>
        <circle class="hitbox"
                r="10"
                pointer-events="all"
                on:click={() => mkFixed()}/>
    </g>
    <line x1={dateScale($dynamicDate?.getTime())}
          x2={dateScale($dynamicDate?.getTime())}
          y1="20"
          y2={height - 45}
          class="dynamic"/>
{/if}


{#if $fixedDate}
    <g class="remove"
       transform="translate({dateScale($fixedDate?.getTime())} 10)">
        <path d={cross} on:click={() => clearFixed()}></path>
        <circle class="hitbox"
                r="10"
                pointer-events="all"
                on:click={() => clearFixed()}/>
    </g>
    <line x1={dateScale($fixedDate?.getTime())}
          x2={dateScale($fixedDate?.getTime())}
          y1="20"
          y2={height - 45}
          class="fixed"/>
{/if}

<style>
    .remove path {
        transform: rotate(45deg);
        fill: red;
    }
    .fix path {
        fill: green;
    }
    .today path {
        fill: #ccc;
    }

    line.dynamic {
        stroke: #888;
        opacity: 0.1;
    }

    line.today {
        stroke: #aaa;
        stroke-dasharray: 2,1;
        opacity: 0.1;
    }

    line.fixed {
        stroke: #ee3f3f;
        opacity: 0.1;
    }

    .hitbox {
        fill: none;
    }
    .hitbox:hover {
        stroke: #ccc;
    }
</style>