<script>

    import {diagramService} from "./entity-diagram-store";
    import _ from "lodash";

    export let cellId;
    export let data;
    export let height;
    export let width;
    export let renderMode;

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
                  {renderMode}
                  {...$overlayProperties}

/>