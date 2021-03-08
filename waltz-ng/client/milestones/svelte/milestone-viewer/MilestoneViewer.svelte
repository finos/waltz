<script>

    import {hsl} from "d3-color";
    import {scaleBand, scaleOrdinal, scaleSqrt, scaleUtc} from "d3-scale";
    import {mouse, select} from "d3-selection";
    import TestData from "../../test-data";

    import Defs from "./Defs.svelte"
    import SubChart from "./SubChart.svelte";
    import Controls from "./Controls.svelte";
    import {calcDateExtent, toStackData} from "../../milestone-utils";
    import DateGuideLines from "./DateGuideLines.svelte";
    import DetailView from "./DetailView.svelte";
    import {measurableStore} from "../../../svelte-stores/measurables";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {dynamicDate} from "./stores/selected-dates";
    import {backgroundColors, commonYScale, dateScale, foregroundColors} from "./stores/decorators";
    import {measurablesById, selectedMeasurable} from "./stores/measurables";
    import {ratingSchemeItems} from "./stores/ratings";
    import SelectedEntityView from "./SelectedEntityView.svelte";

    export let primaryEntityRef = null;

    const width = 400, height = 600;
    const margin = {
        left: 60,
        right: 20,
        top: 20,
        bottom: 20
    };

    let measurables = measurableStore.loadAll();
    let ratingScheme = ratingSchemeStore.getById(47);

    let hitbox;
    let svg;
    let data = TestData;
    let stacks = [];

    $: measurablesById
        .set(_.keyBy($measurables.data, d => d.id));

    $: ratingSchemeItems.set($ratingScheme.data.ratings);

    $: {
        const groupedByVenue = _.groupBy(
            data,
            d => d.id_b);

        stacks = _.map(
            groupedByVenue,
            (v, k) => ({k, stackData: toStackData(v)}));
    }

    $: y = scaleBand()
        .domain(_.map(stacks, d => d.k))
        .range([0, height - (margin.top + margin.bottom)])
        .padding(0.4);

    $dateScale = scaleUtc()
        .domain(calcDateExtent(data, 30 * 12))
        .range([0, width - (margin.left + margin.right)]);


    $: {
        const maxY = _
            .chain(stacks)
            .map(s => s.stackData)
            .flatMap(d => _.map(d, d => d.values))
            .map(d => _.sum(_.map(d, (xs, k) => _.size(xs))))
            .max()
            .value();

        $commonYScale = scaleSqrt()
                .domain([0, maxY]).nice()
                .range([y.bandwidth(), 0]);

        $backgroundColors = scaleOrdinal()
            .domain(_.map($ratingSchemeItems, d => d.id))
            .range(_.map($ratingSchemeItems, d => hsl(d.color).brighter(1.1)))
            .unknown("#eee");

        $foregroundColors = scaleOrdinal()
            .domain(_.map($ratingSchemeItems, d => d.id))
            .range(_.map($ratingSchemeItems, d => hsl(d.color)))
            .unknown("#eee");
    }

    $: {
        const hb = select(hitbox);
        hb.on("click.select", () => {
            const mousePosition = mouse(hb.node())[0];
            const selectedDate = $dateScale.invert(mousePosition);
            dynamicDate.set(selectedDate);
        });
    }

</script>

<div class="row">
    <div class="col-md-12">
        <Controls/>
    </div>
</div>

<div class="row">
    <div class="col-sm-7">
        <svg viewBox="0 0 {width} {height}">
            {#if $backgroundColors && $foregroundColors}
                <Defs />

                <g transform="translate({margin.left} {margin.top})">
                    {#each stacks as subChart}
                        <g transform="translate(0 {y(subChart.k)})">
                            <SubChart data={subChart}
                                      width={width - (margin.left + margin.right)}
                                      height={y.bandwidth()}/>
                        </g>
                    {:else}
                        <text dy="50" dx="10">No Data</text>
                    {/each}
                    <rect bind:this={hitbox}
                          width={width - (margin.left + margin.right)}
                          height={height - (margin.top + margin.bottom)}
                          fill="none"
                          pointer-events="all"/>
                    <DateGuideLines height={height} />


                </g>
            {/if}
        </svg>
    </div>
    <div class="col-sm-5">
        <DetailView data={stacks}/>
    </div>
    {#if $selectedMeasurable}
        <div class="col-sm-5">
            <SelectedEntityView {data}/>
        </div>
    {/if}
</div>

<style>
    svg {
        max-width: 560px;
    }
</style>