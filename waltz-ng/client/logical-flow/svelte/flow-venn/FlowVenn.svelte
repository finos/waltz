<script>

    import {extent} from "d3-array";
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";
    import SingleVenn from "./SingleVenn.svelte";


    function calcSize(flows, dir, grpCode) {
        return _.sumBy(
            filterFlows(flows, dir, grpCode),
            d => d.count);
    }

    function filterFlows(flows, srcGrp, trgGrp) {
        return _.filter(
            flows,
            d => d.source.indexOf(srcGrp) > -1 && d.target.indexOf(trgGrp) > -1);
    }

    const flows =  [
        { source: "A", target: "B", count: 6998 },
        { source: "B", target: "A", count: 409 },
        { source: "A", target: "AnB", count: 222 },
        { source: "AnB", target: "A", count: 292 },
        { source: "B", target: "AnB", count: 282 },
        { source: "AnB", target: "B", count: 332 },
        { source: "AnB", target: "AnB", count: 264 }
    ];

    let ab_a = calcSize(flows, "A", "");
    let ab_b = calcSize(flows, "", "B");
    let ba_a = calcSize(flows, "", "A");
    let ba_b = calcSize(flows, "B", "");

    const ab = {
        a: {name: "CIB key apps", size: 224},
        b: {name: "Legal Holds", size: 319},
        flows: filterFlows(flows, "A", "B"),
        sizes: {
            a: ab_a,
            b: ab_b,
        }
    };

    const ba = {
        a: {name: "cib key apps", size: 224},
        b: {name: "legal holds", size: 319},
        flows: filterFlows(flows, "B", "A"),
        sizes: {
            a: ba_a,
            b: ba_b
        }
    };

    const sizes = [ab_a, ab_b, ba_a, ba_b];
    const ext = extent(sizes);

    const sizeScale = scaleLinear()
        .domain([0, ext[1]])
        .range([0, 200]);

    $: console.log({sizes, ab, ba})

</script>


<SingleVenn label={`${ab.a.name} to ${ab.b.name}`}
            data={ab}
            {sizeScale}/>

<SingleVenn label={`${ab.b.name} to ${ab.a.name}`}
            data={ba}
            {sizeScale}/>

