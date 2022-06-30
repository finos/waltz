<script>
    import {createEventDispatcher, getContext} from "svelte";
    import {displayError} from "../../../../common/error-utils";
    import {aggregateOverlayDiagramStore} from "../../../../svelte-stores/aggregate-overlay-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import {toUpperSnakeCase} from "../../../../common/string-utils";
    import _ from "lodash";

    const focusWidget = getContext("focusWidget");
    const filterParameters = getContext("filterParameters");
    const widgetParameters = getContext("widgetParameters");
    const diagramPresets = getContext("diagramPresets");
    const selectedDiagram = getContext("selectedDiagram");

    let name;
    let description;
    let presetsCall;

    const dispatch = createEventDispatcher();

    function createPreset() {

        const filterConfig = _.map(
            $filterParameters,
            filter => ({
                filterKind: entity.ASSESSMENT_RATING.key,
                filterParameters: filter
            }));

        const overlayConfig = {
            widgetKey: $focusWidget.key,
            widgetParameters: $widgetParameters
        };

        const createCmd = {
            name,
            description,
            diagramId: $selectedDiagram.id,
            externalId: toUpperSnakeCase(name),
            overlayConfig: JSON.stringify(overlayConfig),
            filterConfig: JSON.stringify(filterConfig)
        };

        let createPromise = aggregateOverlayDiagramStore.createPreset(createCmd);

        reloadPresetsForDiagram(createPromise);
    }

    function reloadPresetsForDiagram(createPromise) {
        createPromise
            .then(() => {
                presetsCall = aggregateOverlayDiagramStore.findPresetsForDiagram($selectedDiagram.id, true)
                $diagramPresets = $presetsCall?.data;
            })
            .catch(e => displayError("Could not save preset", e))
            .finally(cancel)
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<div class="help-block">
    <Icon name="info-circle"/>
    Creating a preset will save this overlay and the selected filters for reuse
</div>

<form autocomplete="off"
      on:submit|preventDefault={createPreset}>

    <input class="form-control"
           id="name"
           maxlength="255"
           required="required"
           placeholder="Name"
           bind:value={name}/>
    <div class="help-block">
        Name of the Preset
    </div>

    <textarea class="form-control"
              id="description"
              placeholder="Description"
              bind:value={description}></textarea>
    <div class="help-block">
        Description of this preset
    </div>


    <button type="submit"
            class="btn btn-success"
            disabled={name === null || _.isNull($focusWidget)}>
        Save
    </button>

    <button class="btn skinny"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
</form>