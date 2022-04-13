<script>

    import EntityGroupBox from "./EntityGroupBox.svelte";
    import {scaleLinear} from "d3-scale";
    import {layout} from "./overlay-diagram-builder-utils";



    const channels = { id: "CHANNELS", name: "Channels / Client Care" };
    const cashManagement = { id: "CASH_MGMT", name: "Cash Management" };
    const tradeFinance = { id: "TRADE_FINANCE", name: "Trade Finance" };
    const securitiesServices = { id: "SECURITIES", name: "Securities Services" };

    const surveillance = { id: "SURV", name: "Surveillance", associatedEntities: [{id: 19010, kind: "MEASURABLE"}] };
    const analytics = { id: "REPORTING", name: "Analytics & Reporting" };
    const controls = { id: "CONTROLS", name: "Controls" };
    const billing = { id: "BILLING", name: "Billing" };
    const informationManagement = { id:"INFO_MGMT", name: "Information Management" };

    const afc = { id: "AFC", name: "Anti Financial Crime" };
    const compliance = { id: "COMPLIANCE", name: "Compliance" };
    const finance = { id: "FINANCE", name: "Finance" };
    const risk = { id: "RISK", name: "Risk" };
    const treasury = { id: "TREASURE", name: "Treasury" };

    const accessPoints = {
        id: "ACCESS_POINTS",
        name: "Access Points & Business Lines",
        rows: [
            [ channels ],
            [ cashManagement, tradeFinance, securitiesServices ]
        ]
    };

    const businessManagement = {
        id: "BUS_MGMT",
        name: "Business Management & Operations",
        rows: [
            [ surveillance, analytics],
            [ controls ],
            [ billing, informationManagement ]
        ]
    };

    const controlFunctions = {
        id: "CONTROL_FNS",
        name: "Control Functions / Service Integration",
        rows: [
            [afc, compliance, finance, risk/*, treasury*/]
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