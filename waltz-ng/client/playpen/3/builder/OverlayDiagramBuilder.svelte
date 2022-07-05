<script>

    import EntityGroupBox from "./EntityGroupBox.svelte";
    import {scaleLinear} from "d3-scale";
    import {layout} from "./overlay-diagram-builder-utils";
    import {cellHeight, statsBoxWidth} from "./overlay-diagram-builder-store";

    export let config;

    const dimensions = {
        w: 1000,
        padding: 10,
        labelWidth: 160,
        group: {
            padding: 10
        },
        cell: {
            padding: 10,
            height: 120,
            labelHeight: 40,
        },
        callout: {
            width: 30,
            height: 30
        }
    };

    $: dimensions.cell.height = $cellHeight;
    $: dimensions.cell.statsBoxWidth = $statsBoxWidth;

    let data = [];
    let groupColorScale;

    $: {
        data = layout(
            config,
            dimensions);

        groupColorScale = scaleLinear()
            .domain([0, data.length - 1])
            .range(["#DCEFEB", "#67B9A9"]);
    }


</script>

{#key config}
    <svg width="100%"
         preserveAspectRatio="xMidYMin meet"
         viewBox={`0 0 1000 ${dimensions.height}`}
         style="border: 1px solid #dcefeb">

        <style>
            .data-cell.inset .cell-background {
                filter: url(#inset);
            }

            .entity-group-box.inset .section-header.cell-background {
                filter: url(#header-inset);
            }

            .data-cell .cell-related-entity-indicator {
                visibility: hidden;
            }

            .data-cell.show-related-entity-indicator .cell-related-entity-indicator {
                visibility: visible;
            }

            .data-cell {
                transition: opacity 300ms;
            }

            .data-cell.no-data {
                opacity: 0.5;
            }

        </style>

        <defs>
            <filter id='inset'>
                <!-- Shadow offset -->
                <feOffset
                    dx='0'
                    dy='0'
                />
                <!-- Shadow blur -->
                <feGaussianBlur
                    stdDeviation='10'
                    result='offset-blur'
                />
                <!-- Invert drop shadow to make an inset shadow-->
                <feComposite
                    operator='out'
                    in='SourceGraphic'
                    in2='offset-blur'
                    result='inverse'
                />
                <!-- Cut colour inside shadow -->
                <feFlood
                    flood-color='#21077F'
                    flood-opacity='.95'
                    result='color'
                />
                <feComposite
                    operator='in'
                    in='color'
                    in2='inverse'
                    result='shadow'
                />
                <!-- Placing shadow over element -->
                <feComposite
                    operator='over'
                    in='shadow'
                    in2='SourceGraphic'
                />
            </filter>
            <filter id='header-inset'>
                <!-- Shadow offset -->
                <feOffset
                    dx='0'
                    dy='0'
                />
                <!-- Shadow blur -->
                <feGaussianBlur
                    stdDeviation='10'
                    result='offset-blur'
                />
                <!-- Invert drop shadow to make an inset shadow-->
                <feComposite
                    operator='out'
                    in='SourceGraphic'
                    in2='offset-blur'
                    result='inverse'
                />
                <!-- Cut colour inside shadow -->
                <feFlood
                    flood-color='#BFADFF'
                    flood-opacity='.95'
                    result='color'
                />
                <feComposite
                    operator='in'
                    in='color'
                    in2='inverse'
                    result='shadow'
                />
                <!-- Placing shadow over element -->
                <feComposite
                    operator='over'
                    in='shadow'
                    in2='SourceGraphic'
                />
            </filter>
        </defs>

        <g>
            {#each data as block, idx}
                <EntityGroupBox layoutData={ block.layoutData }
                                {dimensions}
                                color={groupColorScale(idx)}
                                group={block}/>
            {/each}
        </g>
    </svg>
{/key}