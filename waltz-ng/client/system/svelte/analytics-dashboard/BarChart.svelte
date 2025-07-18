<script>
    import * as d3 from "d3";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte"
    export let chartData = [];
    export let xAxisTicks = false;

    let svgContainer;
    let containerWidth;

    $: if (svgContainer && chartData.length > 0 && containerWidth) {
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 30, bottom: 20, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 300 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        const x = d3.scaleBand()
            .domain(chartData.map(d => d.name))
            .range([0, width])
            .padding(0.2);

        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(xAxisTicks ? d3.axisBottom(x) : d3.axisBottom(x).tickFormat(""));

        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData, d => d.value)])
            .range([height, 0]);

        svg.append("g")
            .call(d3.axisLeft(y));

        svg.selectAll("rect")
            .data(chartData)
            .enter()
            .append("rect")
            .attr("x", d => x(d.name))
            .attr("y", d => y(d.value))
            .attr("width", x.bandwidth())
            .attr("height", d => height - y(d.value))
            .attr("fill", "#46baff")
            .append("title")
            .text(d => `${d.name}\n${d.value}`);
    }
</script>
<div bind:clientWidth={containerWidth} style="position: relative;">
    {#if chartData.length === 0}
        <LoadingPlaceholder/>
    {:else}
        <div bind:this={svgContainer}></div>
    {/if}
</div>