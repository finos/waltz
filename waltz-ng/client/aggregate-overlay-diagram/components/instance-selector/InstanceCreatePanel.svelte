<script>
    import {createEventDispatcher, getContext} from "svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";

    export let primaryEntityRef;

    const selectedDiagram = getContext("selectedDiagram");
    let svgDetail = getContext("svgDetail");
    let instances = getContext("instances");
    let instanceCall;

    let name;
    let description;

    const dispatch = createEventDispatcher();

    function createInstance() {
        const svgString = $svgDetail.outerHTML;

        const createCmd = {
            name,
            description,
            diagramId: $selectedDiagram.id,
            parentEntityReference: primaryEntityRef,
            svg: svgString
        }

        let createPromise = aggregateOverlayDiagramInstanceStore.create(createCmd);

        reloadInstances(createPromise);

    }

    function reloadInstances(createPromise) {
        createPromise
            .then(() => {
                instanceCall = aggregateOverlayDiagramInstanceStore.findByDiagramId($selectedDiagram.id, true)
                $instances = instanceCall?.data;
            })
            .catch(e => displayError("Could not save diagram", e))
            .finally(cancel)
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<h4>Create a diagram instance:</h4>

<div class="help-block">
    <Icon name="info-circle"/>
    Creating a diagram will save the data relevant to this vantage point and the overlays selected
</div>

<form autocomplete="off"
      on:submit|preventDefault={createInstance}>

    <input class="form-control"
           id="name"
           maxlength="255"
           required="required"
           placeholder="Name"
           bind:value={name}/>
    <div class="help-block">
        Name of the Diagram Instance
    </div>

    <textarea class="form-control"
              id="description"
              placeholder="Description"
              bind:value={description}/>
    <div class="help-block">
        Description of this diagram instance
    </div>


    <button type="submit"
            class="btn btn-success"
            disabled={name === null}>
        Save
    </button>

    <button class="btn skinny"
            on:click={cancel}>
        Cancel
    </button>
</form>