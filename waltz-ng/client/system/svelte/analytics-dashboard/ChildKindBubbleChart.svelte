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
        console.log('ChildKindBubbleChart loading data with key:', key, 'period:', period);
        try {
            const url = `/api/change-log-summaries/analytics/by-child-kind?startDate=${startDate}&endDate=${endDate}&limit=15`;
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

            // Check if data is empty or null
            if (!data || Object.keys(data).length === 0) {
                chartData = {
                    title: "Changes by Child Entity Kind",
                    data: []
                };
                loading = false;
                return;
            }

            // Transform the data for the bubble chart
            chartData = {
                title: "Changes by Child Entity Kind",
                data: Object.entries(data)
                    .filter(([childKind, count]) => childKind && childKind !== 'null' && count > 0)
                    .map(([childKind, count]) => ({
                        name: childKind,
                        value: count,
                        displayName: formatChildKind(childKind)
                    }))
            };
            loading = false;
        } catch (err) {
            console.error('Child Kind Bubble API failed:', err);
            console.error('Response status:', response?.status);
            console.error('Error details:', err);
            error = `Failed to load child kind data: ${err.message}`;
            loading = false;
        }
    }

    function formatChildKind(childKind) {
        if (!childKind) return 'Unknown';
        return childKind.split('_')
                        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
                        .join(' ');
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

        const margin = {top: 40, right: 40, bottom: 40, left: 40};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Create a hierarchy for the pack layout
        const root = d3.hierarchy({children: chartData.data})
            .sum(d => d.value)
            .sort((a, b) => b.value - a.value);

        // Create pack layout
        const pack = d3.pack()
            .size([width, height])
            .padding(5);

        pack(root);

        // Color scale with child kind specific colors
        const colorScale = d3.scaleOrdinal()
            .domain(chartData.data.map(d => d.name))
            .range(d3.schemeTableau10);

        // Create bubbles
        const node = svg.selectAll(".node")
            .data(root.leaves())
            .enter().append("g")
            .attr("class", "node")
            .attr("transform", d => `translate(${d.x},${d.y})`);

        // Add circles with safe styling
        const circles = node.append("circle")
            .attr("r", d => d.r)
            .attr("fill", d => colorScale(d.data.name))
            .attr("stroke", "#fff")
            .attr("stroke-width", 2)
            .attr("opacity", 0.8);

        // Apply styles safely
        circles.each(function() {
            if (this && this.parentNode) {
                d3.select(this).style("cursor", "pointer");
            }
        })
            .on("mouseover", function(event, d) {
                d3.select(this).attr("opacity", 1).attr("stroke-width", 3);

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
                    .html(`<strong>${d.data.displayName}</strong><br/>Child Kind: ${d.data.name}<br/>Changes: ${d.data.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("opacity", 0.8).attr("stroke-width", 2);
                d3.selectAll(".tooltip").remove();
            });

        // Add text labels
        node.append("text")
            .selectAll("tspan")
            .data(d => {
                const radius = d.r;
                if (radius < 20) return []; // Don't show text for very small bubbles

                const lines = [];

                // Split long names into multiple lines
                const words = d.data.displayName.split(' ');
                if (words.length > 2 && radius > 35) {
                    // Multiple lines for large bubbles
                    const mid = Math.ceil(words.length / 2);
                    lines.push(words.slice(0, mid).join(' '));
                    lines.push(words.slice(mid).join(' '));
                } else if (radius > 25) {
                    lines.push(d.data.displayName);
                } else {
                    // Abbreviated for smaller bubbles
                    lines.push(words.map(w => w.charAt(0)).join(''));
                }

                // Add count if there's space
                if (radius > 30) {
                    lines.push(d.data.value.toLocaleString());
                }

                return lines;
            })
            .enter().append("tspan")
            .attr("x", 0)
            .attr("y", function(d, i, nodes) {
                try {
                    const node = nodes[i];
                    if (!node || !node.parentNode) return 0;
                    const parent = d3.select(node.parentNode).datum();
                    if (!parent || !parent.r) return 0;
                    const lineHeight = parent.r > 35 ? 12 : 10;
                    const totalHeight = nodes.length * lineHeight;
                    return (i * lineHeight) - (totalHeight / 2) + lineHeight;
                } catch (e) {
                    return 0;
                }
            })
            .attr("text-anchor", "middle")
            .style("font-size", function(d) {
                try {
                    const node = this;
                    if (!node || !node.parentNode) return "9px";
                    const parent = d3.select(node.parentNode).datum();
                    if (!parent || !parent.r) return "9px";
                    if (parent.r > 45) return "11px";
                    if (parent.r > 25) return "9px";
                    return "8px";
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
            .attr("y", -15)
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Changes by Child Entity Kind (Bubble Size = Change Count)");

        // Add legend with top entities
        const topEntities = chartData.data.slice(0, 5);
        const legend = svg.append("g")
            .attr("transform", `translate(${width - 180}, 20)`);

        legend.append("text")
            .attr("x", 0)
            .attr("y", 0)
            .style("font-size", "12px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Top 5 Child Entity Types:");

        const legendItems = legend.selectAll(".legend-item")
            .data(topEntities)
            .enter().append("g")
            .attr("class", "legend-item")
            .attr("transform", (d, i) => `translate(0, ${(i + 1) * 18})`);

        legendItems.append("circle")
            .attr("r", 6)
            .attr("fill", d => colorScale(d.name))
            .attr("stroke", "#fff");

        legendItems.append("text")
            .attr("x", 12)
            .attr("y", 4)
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => `${d.displayName}: ${d.value.toLocaleString()}`);
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Changes by Child Entity Kind"}
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
