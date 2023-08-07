<script>

    import {diagramService} from "./entity-diagram-store";
    import _ from "lodash";
    import {defaultOverlay} from "./entity-diagram-utils";

    export let cellId;
    export let data;
    export let height;
    export let width;
    export let renderMode;

    let comp;

    const {selectedOverlay, overlayData, overlayProperties} = diagramService;

    $: cellData = _.get($overlayData, cellId);

    $: comp = $selectedOverlay
        ? $selectedOverlay.component
        : defaultOverlay.component;

</script>

<svelte:component this={comp}
                  {cellData}
                  {data}
                  {height}
                  {width}
                  {renderMode}
                  {...$overlayProperties}/>
