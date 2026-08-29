<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount } from "svelte";

    export let startDate;
    export let endDate;
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;

    $: if (key) {
        loadData();
    }

    onMount(() => {
        loadData();
    });

    async function loadData() {
        loading = true;
        error = null;
        try {
            const url = `/api/change-log-summaries/analytics/by-severity?startDate=${startDate}&endDate=${endDate}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            console.log('SeverityDonutChart received data:', data);
            console.log('SeverityDonutChart date range:', startDate, 'to', endDate);

            // Check if data is empty or has no valid entries
            if (!data || Object.keys(data).length === 0) {
                console.warn('SeverityDonutChart: No data received from API');
                chartData = { title: "Changes by Severity Level", data: [] };
                loading = false;
                return;
            }

            // Transform the data for the donut chart, filtering out zero values, null keys, and invalid data
            const allEntries = Object.entries(data);
            console.log('SeverityDonutChart: Processing', allEntries.length, 'entries');

            const transformedData = allEntries
                .filter(([severity, count]) => {
                    // Filter out null/undefined severity, invalid counts, and zero values
                    const isValid = severity &&
                           severity !== 'null' &&
                           severity !== 'undefined' &&
                           count &&
                           count > 0 &&
                           typeof count === 'number';

                    if (!isValid) {
                        console.warn('SeverityDonutChart: Filtered out invalid entry:', { severity, count, severityType: typeof severity, countType: typeof count });
                    }

                    return isValid;
                })
                .map(([severity, count]) => ({
                    name: severity,
                    value: count,
                    label: formatSeverity(severity)
                }));

            console.log('SeverityDonutChart: Transformed data:', transformedData);

            chartData = {
                title: "Changes by Severity Level",
                data: transformedData
            };
            loading = false;
        } catch (err) {
            console.error('Severity Donut API failed:', err);
            error = 'Failed to load severity data';
            loading = false;
        }
    }

    function formatSeverity(severity) {
        if (!severity || typeof severity !== 'string') {
            return 'Unknown';
        }
        return severity.charAt(0).toUpperCase() + severity.slice(1).toLowerCase();
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        // Clear previous chart
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 120, bottom: 20, left: 20};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;
        const radius = Math.min(width, height) / 2;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left + width/2},${margin.top + height/2})`);

        // Color scale with severity-appropriate colors
        const colorScale = d3.scaleOrdinal()
            .domain(chartData.data.map(d => d.name))
            .range(['#28a745', '#ffc107', '#dc3545']); // Green, Yellow, Red

        // Pie generator
        const pie = d3.pie()
            .value(d => d.value)
            .sort(null);

        // Arc generator
        const arc = d3.arc()
            .innerRadius(radius * 0.4) // Creates donut hole
            .outerRadius(radius * 0.8);

        const outerArc = d3.arc()
            .innerRadius(radius * 0.9)
            .outerRadius(radius * 0.9);

        // Create arcs
        const arcs = svg.selectAll(".arc")
            .data(pie(chartData.data))
            .enter().append("g")
            .attr("class", "arc");

        // Add paths
        arcs.append("path")
            .attr("d", arc)
            .attr("fill", d => colorScale(d.data.name))
            .attr("stroke", "#fff")
            .attr("stroke-width", 2)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("opacity", 0.8);

                const tooltip = d3.select("body").append("div")
                    .attr("class", "tooltip")
                    .style("position", "absolute")
                    .style("background", "rgba(0,0,0,0.9)")
                    .style("color", "white")
                    .style("padding", "10px")
                    .style("border-radius", "6px")
                    .style("font-size", "13px")
                    .style("pointer-events", "none")
                    .style("z-index", "1000")
                    .style("box-shadow", "0 4px 8px rgba(0,0,0,0.3)")
                    .html(`<strong>${d.data.label}</strong><br/>Count: ${d.data.value.toLocaleString()}<br/>Percentage: ${((d.data.value / d3.sum(chartData.data, d => d.value)) * 100).toFixed(1)}%`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("opacity", 1);
                d3.selectAll(".tooltip").remove();
            });

        // Add percentage labels on slices
        arcs.append("text")
            .attr("transform", d => `translate(${arc.centroid(d)})`)
            .attr("text-anchor", "middle")
            .style("font-size", "12px")
            .style("font-weight", "bold")
            .style("fill", "white")
            .text(d => {
                const percentage = ((d.data.value / d3.sum(chartData.data, d => d.value)) * 100);
                return percentage > 5 ? `${percentage.toFixed(1)}%` : ''; // Only show if > 5%
            });

        // Add center text
        const total = d3.sum(chartData.data, d => d.value);
        svg.append("text")
            .attr("text-anchor", "middle")
            .attr("dy", "-0.5em")
            .style("font-size", "18px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text(total.toLocaleString());

        svg.append("text")
            .attr("text-anchor", "middle")
            .attr("dy", "0.8em")
            .style("font-size", "12px")
            .style("fill", "#666")
            .text("Total Changes");

        // Add legend
        const legend = svg.append("g")
            .attr("transform", `translate(${radius + 20}, ${-chartData.data.length * 15 / 2})`);

        const legendItems = legend.selectAll(".legend-item")
            .data(chartData.data)
            .enter().append("g")
            .attr("class", "legend-item")
            .attr("transform", (d, i) => `translate(0, ${i * 25})`);

        legendItems.append("rect")
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", d => colorScale(d.name))
            .attr("stroke", "#ccc");

        legendItems.append("text")
            .attr("x", 20)
            .attr("y", 12)
            .style("font-size", "12px")
            .style("fill", "#333")
            .text(d => `${d.label}: ${d.value.toLocaleString()}`);
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Changes by Severity Level"}
    </div>
    <div slot="content">
        <div bind:clientWidth={containerWidth} style="position: relative; min-height: 400px;">
            {#if loading}
                <LoadingPlaceholder/>
            {:else if error}
                <div class="alert alert-danger">
                    <i class="fa fa-warning"></i>
                    {error}
                </div>
            {:else if !chartData || !chartData.data || chartData.data.length === 0}
                <NoData/>
            {:else}
                <div bind:this={svgContainer}></div>
            {/if}
        </div>
    </div>
</SubSection>

<style>
    :global(.tooltip) {
        opacity: 0.9;
    }
</style>


