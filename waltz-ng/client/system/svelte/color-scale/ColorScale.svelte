<script>
    import _ from "lodash";
    import {scaleLinear} from "d3-scale";
    import {color} from "d3-color";
    import ColorPicker from "../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {rgbToHex} from "../../../common/colors";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";

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


<PageHeader icon="paint-brush"
            name="Colour Gradient">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Colour Graident</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>This page can be used to create a smooth color gradient with a specified number of steps between a
                start and end colour.
            </p>
            <p>
                A typical use for this is to provide colours for rating schemes which have ordinal values.
            </p>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-6">
            <label>Rows ({howMany}):
                <input type="range"
                       min="0"
                       max="50"
                       bind:value={howMany}>
            </label>
            <br>
            <label for="start-color">
                Start Color
            </label>
            <ColorPicker id="start-color"
                         startColor={'#5bb65d'}
                         on:select={evt => startColor = evt.detail}/>
            <br>
            <label for="end-color">
                End Color
            </label>
            <ColorPicker id="end-color"
                         startColor={'#dae714'}
                         on:select={evt => endColor = evt.detail}/>
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
</div>


<style>
    .box {
        width: 1em;
        height: 1em;
    }
</style>