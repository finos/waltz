<script>

    import EntityGroupBox from "./EntityGroupBox.svelte";
    import {scaleLinear} from "d3-scale";
    import {calcGroupHeight, calcHeight, calcTotalHeight, layout} from "./overlay-diagram-utils";



    const channels = { name: "Channels / Client Care" };
    const cashManagement = { name: "Cash Management" };
    const tradeFinance = { name: "Trade Finance" };
    const securitiesServices = { name: "Securities Services" };

    const surveillance = { name: "Surveillance" };
    const analytics = { name: "Analytics & Reporting" };
    const controls = { name: "Controls" };
    const billing = { name: "Billing" };
    const informationManagement = { name: "Information Management" };

    const afc = { name: "AFC" };
    const compliance = { name: "Compliance" };
    const finance = { name: "Finance" };
    const risk = { name: "Risk" };

    const accessPoints = {
        name: "Access Points & Business Lines",
        rows: [
            [ channels ],
            [ cashManagement, tradeFinance, securitiesServices ]
        ]
    };

    const businessManagement = {
        name: "Business Management & Operations",
        rows: [
            [ surveillance, analytics],
            [ controls ],
            [ billing, informationManagement ]
        ]
    };

    const controlFunctions = {
        name: "Control Functions / Service Integration",
        rows: [
            [afc, compliance, finance, risk]
        ]
    }

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
            statsHeight: 60
        }
    };



    const data = layout(
        [accessPoints, businessManagement, controlFunctions],
        dimensions);

    const groupColorScale = scaleLinear()
        .domain([0, data.length - 1])
        .range(["#DCEFEB", "#67B9A9"]);


    console.log({dimensions, data});

</script>

<svg width="1000px"
     height={`${dimensions.height}px`}
     viewBox={`0 0 1000 ${dimensions.height}`}
     style="border: 1px solid #dcefeb">

    {#each data as block, idx}
        <g transform={`translate(10, ${block.layoutData.dy})`}>
            <EntityGroupBox height={ block.layoutData.height }
                            {dimensions}
                            color={groupColorScale(idx)}
                            group={block}/>
        </g>
    {/each}
</svg>