<script>
    import {backgroundColors, commonYScale, dateScale, foregroundColors, useCommonYScale} from "./stores/decorators";
    import {scaleLinear} from "d3-scale";
    import {max} from "d3-array";
    import {axisBottom, axisLeft} from "d3-axis";
    import _ from "lodash";
    import {select} from "d3-selection";
    import {stack} from "d3-shape";


    const ANIMATION_DURATION = 400;


    export let data;
    export let height = 100;
    export let width = 100;
    export let config;

    let measurablesById;
    let ratings

    $: measurablesById = config.measurablesById;
    $: ratings = config.ratingSchemeItems;

    let el;

    $: keys = _.map(ratings, d => d.id);
    $: subChartName = measurablesById[data?.k]?.name || "-";

    $: {
        const {k, stackData} = data;

        const stacker = stack()
            .keys(_.map(ratings, d => d.id))
            .value((d, k) => _.size(d.values[k]))

        const series = stacker(stackData);

        const svg = select(el);

        const y = $useCommonYScale
            ? $commonYScale
            : scaleLinear()
                .domain([0, max(series, d => max(d, d => d[1])) + 2]).nice()
                .range([height, 0]);

        const x = $dateScale;

        const xAxis = g => g
            .attr("transform", `translate(0 ${height})`)
            .call(axisBottom(x).ticks(width / 80).tickSize(3))
            .call(g => g.selectAll(".tick text").style("fill", "#aaa"))
            .call(g => g.select(".domain").remove());

        const yAxis = g => g
            .call(axisLeft(y).ticks(height / 20).tickSize(3))
            .call(g => g.selectAll(".tick text").style("fill", "#aaa"))
            .call(g => g.select(".domain").remove());

        const ratingBands = svg
            .select(".graph")
            .selectAll("g.rating-band")
            .data(series);

        const newRatingBands = ratingBands
            .enter()
            .append("g")
            .classed("rating-band", true);

        const ratingBandBars = ratingBands
            .merge(newRatingBands)
            .selectAll("rect")
            .data(d => d);

        const newRatingBandBars = ratingBandBars
            .enter()
            .append("rect")
            .attr("stroke", "none")
            .attr("stroke-width", 0.2)
            .attr("x", d => x(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => x(d.data.e || x.domain()[1]) - x(d.data.s))

        ratingBandBars
            .merge(newRatingBandBars)
            .classed("fade-out", d => _.isUndefined(d.data.e))
            .transition()
            .duration(ANIMATION_DURATION)
            .attr("x", d => x(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => x(d.data.e || x.domain()[1]) - x(d.data.s))
            .attr("fill", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#gradient-bg-${p.key})`
                    : $backgroundColors(p.key);
            })
            .attr("stroke", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#gradient-fg-${p.key})`
                    : $foregroundColors(p.key);
            });

        svg.select("g.x-axis")
            .call(xAxis);

        svg.select("g.y-axis")
            .call(yAxis);
    }

</script>

<g class="subChart"
   style="pointer-events: all"
   bind:this={el}>


    <g class="graph"></g>
    <g class="x-axis"></g>
    <g class="y-axis"></g>

    <text dx={width} text-anchor="end" class="subchart-title">
        {subChartName}
    </text>

</g>

<style>
    .subchart-title {
        font-size: 12px;
        fill: #556666;
    }
</style>