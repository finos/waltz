<script>
    import _ from "lodash";
    import {scaleLinear} from "d3-scale";
    import {color} from "d3-color";
    import ColorPicker from "../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {rgbToHex} from "../../common/colors";

    let rows = [];
    let howMany = 5;
    let colors = null;
    let startColor = "#5bb65d";
    let endColor = "#dae714";

    function swap() {
        const temp = startColor;
        startColor = endColor;
        endColor = temp;
    }

    $: colors = scaleLinear()
        .domain([0, howMany])
        .range([startColor, endColor]);

    $: rows = _.range(0, howMany).map(d => {
        const colorRgb = color(colors(d));
        return {
            colorRgb,
            colorHex: rgbToHex(colorRgb.r, colorRgb.g, colorRgb.b)
        };
    });
</script>

<div class="row">
    <div class="col-sm-6">
        <label>Rows ({howMany}):
            <input type="range"
                   min="0"
                   max="30"
                   bind:value={howMany}>
        </label>
        <br>
        <label>Start Color
            <ColorPicker startColor={'#5bb65d'}
                         on:select={evt => startColor = evt.detail}/>
        </label>
        <br>
        <label>End Color
            <ColorPicker startColor={'#dae714'}
                         on:select={evt => endColor = evt.detail}/>
        </label>
    </div>

    <div class="col-sm-6">
        <table class="table table-condensed table-striped">
            {#each rows as row}
                <tr>
                    <td>
                        <div class="box"
                             style={`background-color: ${row.colorRgb}`}>
                        </div>
                    </td>
                    <td><span>{row.colorHex}</span></td>
                </tr>
            {/each}
        </table>

        <button class="btn"
                on:click={swap}>
            Swap Direction
        </button>
    </div>
</div>


<style>
    .box {
        width: 1em;
        height: 1em;
    }
</style>