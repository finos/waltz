<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount, onDestroy, tick } from "svelte";

    export let startDate;
    export let endDate;
    export let period = "month"; // Optional - not used by this chart's API
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;
    let lastLoadKey = 0;

    // Load data when key changes
    $: if (key && startDate && endDate) {
        loadData();
    }

    onMount(() => {
        if (startDate && endDate) {
            loadData();
        }
    });

    onDestroy(() => {
        if (svgContainer) {
            try {
                d3.select(svgContainer).selectAll("*").remove();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    });

    async function loadData() {
        if (!startDate || !endDate) {
            loading = false;
            return;
        }

        // Simple duplicate prevention
        const currentKey = key;
        if (currentKey === lastLoadKey) {
            return;
        }
        lastLoadKey = currentKey;

        loading = true;
        error = null;
        chartData = null;
        console.log('OperationsDonutChart loading data with key:', key, 'period:', period);
        try {
            const url = `/api/change-log-summaries/analytics/by-operation?startDate=${startDate}&endDate=${endDate}`;
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

            // Check if data is empty or has no valid entries
            if (!data || Object.keys(data).length === 0) {
                chartData = { title: "Changes by Operation Type", data: [] };
                loading = false;
                return;
            }

            // Transform the data for the donut chart, filtering out zero values
            const transformedData = Object.entries(data)
                .filter(([operation, count]) => count > 0)
                .map(([operation, count]) => ({
                    name: operation,
                    value: count,
                    label: formatOperation(operation)
                }));

            chartData = {
                title: "Changes by Operation Type",
                data: transformedData
            };
            loading = false;
        } catch (err) {
            console.error('Operations Donut API failed:', err);
            error = 'Failed to load operations data';
            loading = false;
        }
    }

    function formatOperation(operation) {
        if (!operation) return 'Unknown';
        return operation.charAt(0).toUpperCase() + operation.slice(1).toLowerCase();
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.data) {
            return;
        }

        // Clear previous chart safely
        try {
            const container = d3.select(svgContainer);
            if (!container.empty()) {
                container.selectAll("svg").remove();
            }
        } catch (e) {
            console.warn('Error clearing chart:', e);
        }

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

        // Color scale with operation-appropriate colors
        const colorScale = d3.scaleOrdinal()
            .domain(chartData.data.map(d => d.name))
            .range(['#28a745', '#007bff', '#dc3545', '#6c757d']); // Green (ADD), Blue (UPDATE), Red (REMOVE), Gray (UNKNOWN)

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
            .text("Total Operations");

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
        {chartData?.title || "Changes by Operation Type"}
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
                <div class="alert alert-info text-center">
                    <i class="fa fa-info-circle"></i>
                    No data found
                </div>
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
