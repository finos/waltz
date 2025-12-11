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
        console.log('TopContributorsTreemap loading data with key:', key, 'period:', period);
        try {
            // Use period-aware API when period is provided and not default
            const shouldUsePeriod = period && period !== 'month';
            const url = shouldUsePeriod
                ? `/api/change-log-summaries/analytics/top-contributors-trends?startDate=${startDate}&endDate=${endDate}&period=${period}&limit=20`
                : `/api/change-log-summaries/analytics/top-contributors?startDate=${startDate}&endDate=${endDate}&limit=20`;
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
                chartData = {
                    title: shouldUsePeriod
                        ? `Top Contributors by Change Count (${period.charAt(0).toUpperCase() + period.slice(1)}ly)`
                        : "Top Contributors by Change Count",
                    data: { name: "Contributors", children: [] }
                };
                loading = false;
                return;
            }

            let transformedData;
            if (shouldUsePeriod) {
                // Handle period-based data: { "user1": { "2024-01": 10, "2024-02": 15 }, ... }
                transformedData = Object.entries(data)
                    .map(([userId, periods]) => {
                        const totalCount = Object.values(periods).reduce((sum, count) => sum + count, 0);
                        return {
                            name: userId,
                            value: totalCount,
                            displayName: formatUserName(userId),
                            periods: periods // Keep period data for potential future use
                        };
                    })
                    .filter(item => item.value > 0);
            } else {
                // Handle simple data: { "user1": 25, "user2": 20, ... }
                transformedData = Object.entries(data)
                    .filter(([userId, count]) => count > 0)
                    .map(([userId, count]) => ({
                        name: userId,
                        value: count,
                        displayName: formatUserName(userId)
                    }));
            }

            // Transform the data for the treemap
            chartData = {
                title: shouldUsePeriod
                    ? `Top Contributors by Change Count (${period.charAt(0).toUpperCase() + period.slice(1)}ly)`
                    : "Top Contributors by Change Count",
                data: {
                    name: "Contributors",
                    children: transformedData
                }
            };
            loading = false;
        } catch (err) {
            console.error('Top Contributors API failed:', err);
            error = 'Failed to load top contributors data';
            loading = false;
        }
    }

    function formatUserName(userId) {
        if (!userId) return 'Unknown User';
        // Remove common prefixes and format nicely
        return userId.replace(/^(user|usr|u)[-_]?/i, '')
                   .replace(/[-_]/g, ' ')
                   .split(' ')
                   .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
                   .join(' ');
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.children && chartData.data.children.length > 0 && containerWidth) {
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

        const margin = {top: 20, right: 20, bottom: 20, left: 20};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Create hierarchy and treemap
        const root = d3.hierarchy(chartData.data)
            .sum(d => d.value)
            .sort((a, b) => b.value - a.value);

        const treemap = d3.treemap()
            .size([width, height])
            .paddingInner(2)
            .paddingOuter(2)
            .round(true);

        treemap(root);

        // Color scale
        const colorScale = d3.scaleSequential()
            .domain([0, d3.max(root.leaves(), d => d.data.value)])
            .interpolator(d3.interpolateViridis);

        // Create treemap rectangles
        const leaf = svg.selectAll("g")
            .data(root.leaves())
            .enter().append("g")
            .attr("transform", d => `translate(${d.x0},${d.y0})`);

        leaf.append("rect")
            .attr("id", d => `rect-${d.data.name}`)
            .attr("width", d => d.x1 - d.x0)
            .attr("height", d => d.y1 - d.y0)
            .attr("fill", d => colorScale(d.data.value))
            .attr("stroke", "#fff")
            .attr("stroke-width", 1)
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
                    .html(`<strong>${d.data.displayName}</strong><br/>User ID: ${d.data.name}<br/>Changes: ${d.data.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("opacity", 1);
                d3.selectAll(".tooltip").remove();
            });

        // Add text labels
        leaf.append("clipPath")
            .attr("id", d => `clip-${d.data.name}`)
            .append("use")
            .attr("xlink:href", d => `#rect-${d.data.name}`);

        leaf.append("text")
            .attr("clip-path", d => `url(#clip-${d.data.name})`)
            .selectAll("tspan")
            .data(d => {
                const rectWidth = d.x1 - d.x0;
                const rectHeight = d.y1 - d.y0;

                // Only show text if rectangle is large enough
                if (rectWidth < 60 || rectHeight < 30) return [];

                const lines = [];

                // Add display name
                if (rectWidth > 80) {
                    lines.push(d.data.displayName);
                }

                // Add count if there's space
                if (rectHeight > 45) {
                    lines.push(d.data.value.toLocaleString());
                }

                return lines;
            })
            .enter().append("tspan")
            .attr("x", 4)
            .attr("y", (d, i) => 13 + i * 14)
            .style("font-size", function(d, i, nodes) {
                try {
                    const node = nodes[i];
                    if (!node || !node.parentNode) return "9px";
                    const parent = d3.select(node.parentNode).datum();
                    if (!parent || typeof parent.x1 === 'undefined' || typeof parent.x0 === 'undefined') return "9px";
                    const rectWidth = parent.x1 - parent.x0;
                    return rectWidth > 100 ? "11px" : "9px";
                } catch (e) {
                    return "9px";
                }
            })
            .style("font-weight", (d, i) => i === 0 ? "bold" : "normal")
            .style("fill", "white")
            .style("text-shadow", "1px 1px 1px rgba(0,0,0,0.8)")
            .text(d => d);

        // Add title
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", -5)
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Top Contributors by Change Count");

        // Add legend
        const legendWidth = 200;
        const legendHeight = 10;
        const legend = svg.append("g")
            .attr("transform", `translate(${width - legendWidth}, ${height + 5})`);

        // Create gradient for legend
        const defs = svg.append("defs");
        const gradient = defs.append("linearGradient")
            .attr("id", "treemap-legend-gradient");

        const maxValue = d3.max(root.leaves(), d => d.data.value);
        gradient.selectAll("stop")
            .data(d3.range(0, 1.1, 0.1))
            .enter().append("stop")
            .attr("offset", d => `${d * 100}%`)
            .attr("stop-color", d => colorScale(d * maxValue));

        // Legend rectangle
        legend.append("rect")
            .attr("width", legendWidth)
            .attr("height", legendHeight)
            .style("fill", "url(#treemap-legend-gradient)")
            .attr("stroke", "#ccc");

        // Legend labels
        legend.append("text")
            .attr("x", 0)
            .attr("y", legendHeight + 12)
            .style("font-size", "10px")
            .style("fill", "#666")
            .text("0");

        legend.append("text")
            .attr("x", legendWidth)
            .attr("y", legendHeight + 12)
            .attr("text-anchor", "end")
            .style("font-size", "10px")
            .style("fill", "#666")
            .text(`${maxValue.toLocaleString()} changes`);
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Top Contributors"}
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
            {:else if !chartData || !chartData.data || !chartData.data.children || chartData.data.children.length === 0}
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


