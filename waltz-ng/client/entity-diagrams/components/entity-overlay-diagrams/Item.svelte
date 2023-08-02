<script>

    import {diagramService} from "./entity-diagram-store";
    import _ from "lodash";

    export let cellId;
    export let data;
    export let height = 100;
    export let width = 100;

    let comp;

    const {selectedOverlay, overlayData, overlayProperties} = diagramService;

    $: cellData = _.get($overlayData, cellId);

    $: comp = $selectedOverlay.component;

    $: console.log({So: $selectedOverlay, cellData, od: $overlayData, op: $overlayProperties});

</script>


<svelte:component this={comp}
                  {cellData}
                  {data}
                  {height}
                  {width}
                  {...$overlayProperties}

/>