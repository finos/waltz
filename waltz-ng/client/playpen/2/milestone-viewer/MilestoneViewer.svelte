<script>

import {scaleLinear, scaleOrdinal} from "d3-scale";
import {axisBottom, axisLeft} from "d3-axis";
import {select, selectAll} from "d3-selection";
import {stack} from "d3-shape"; import {max} from "d3-array";
import Defs from "./Defs.svelte"

const width = 400, height = 300;


const margin = {
    left: 20,
    right: 20,
    top: 20,
    bottom: 20
};


let data = [];
let el;
let svg;

let subCharts = [];


</script>

<h1>HELLO</h1>

<div class="row">
    <div class="col-md-6">
        <svg bind:this={el} viewBox="0 0 {width} {height}">
            <Defs {colors}>
            </Defs>
            {#each subCharts as subChart}
            <g>
                <SubChart data="subChart"></SubChart>
            </g>
            {:else}
            <text dy="50" dx="10">No Data</text>
            {/each}
        </svg>
    </div>
    <div class="col-md-6">
        <h3>Controls</h3>

        <label>
            Blur deviation ({blurDeviation || "-"})
        </label>
        <input type="range" min="0.1" max="10" step="0.1" bind:value={blurDeviation}/>

        <label>
            Use blur
            <input type="checkbox" bind:checked={useBlur}/>
        </label>


        <label>
            Show Axes
            <input type="checkbox" bind:checked={showAxes}/>
        </label>
    </div>
</div>

<style>
</style>