<script>

    import {diagramService} from "./entity-diagram-store";
    import _ from "lodash";

    export let cellId;
    export let data;

    let comp;

    const {selectedOverlay, overlayData} = diagramService;

    $: comp = $selectedOverlay.component;

    $: cellData = _.get(overlayData, cellId)

    $: console.log({overlayData: $overlayData, selectedOverlay: $selectedOverlay});


</script>


<svelte:component this={comp}
                  {cellData}
                  {data}/>