<script>


    import {aggregateOverlayDiagramCalloutStore} from "../../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ColorPicker from "../../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {createEventDispatcher, getContext} from "svelte";
    import {calloutColors} from "../aggregate-overlay-diagram-utils";
    import {displayError} from "../../../../common/error-utils";

    let selectedInstance = getContext("selectedInstance");
    let svgDetail = getContext("svgDetail");
    let selectedCellId = getContext("selectedCellId");
    let selectedCellCallout = getContext("selectedCellCallout");
    let callouts = getContext("callouts");

    let requireSecondColor = false;
    let calloutsCall;

    const dispatch = createEventDispatcher();

    let workingCallout = Object.assign({}, $selectedCellCallout);

    function cancel() {
        dispatch("cancel");
    }

    function onSelectStartColor(evt) {
        workingCallout.startColor = evt.detail;
    }

    function onSelectEndColor(evt) {
        workingCallout.endColor = evt.detail;
    }

    function save() {
        if (workingCallout.id) {
            updateCallout();
        } else {
            createCallout();
        }
    }

    function determineEndColor(startColor, endColor, requireSecondColor, defaultColor) {
        if (requireSecondColor && endColor) {
            return endColor;
        } else if (requireSecondColor) {
            return defaultColor;
        } else if (startColor) {
            return startColor;
        } else {
            return defaultColor;
        }
    }

    function createCallout() {
        let startColor = workingCallout.startColor ? workingCallout.startColor : defaultColor;
        let endColor = determineEndColor(workingCallout.startColor, workingCallout.endColor, requireSecondColor, defaultColor);

        const createCommand = Object.assign(
            {},
            workingCallout,
            {
                startColor,
                endColor,
                cellExternalId: $selectedCellId,
                instanceId: $selectedInstance.id
            });

        let savePromise = aggregateOverlayDiagramCalloutStore.create(createCommand);

        reloadCallouts(savePromise)
    }

    function updateCallout() {
        const updateCommand = Object.assign(
            {},
            workingCallout,
            {
                endColor: requireSecondColor ? workingCallout.endColor : workingCallout.startColor,
                cellExternalId: $selectedCellId,
                diagramInstanceId: $selectedInstance.id
            });

        let savePromise = aggregateOverlayDiagramCalloutStore.update(updateCommand);

        reloadCallouts(savePromise);
    }

    function reloadCallouts(savePromise) {

        savePromise
            .then(() => {
                calloutsCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id, true);
                $callouts = $calloutsCall?.data;
            })
            .catch(e => displayError("Could not save callout", e))
            .finally(cancel)
    }

    $: defaultColor = _.first(calloutColors);

    $: invalid = _.isNil(workingCallout.title) || _.isNil(workingCallout.content)

</script>

<h4>Adding a callout:</h4>
{#if !_.isNil($selectedCellId)}
    <form autocomplete="off"
          on:submit|preventDefault={save}>

        <input class="form-control"
               id="name"
               maxlength="255"
               required="required"
               placeholder="Title"
               bind:value={workingCallout.title}/>
        <div class="help-block">
            The title of this callout
        </div>

        <textarea class="form-control"
                  id="content"
                  placeholder="Content"
                  bind:value={workingCallout.content}/>
        <div class="help-block">
            The main content of this callout, markdown is supported
        </div>

        <div id="start-color">
            <ColorPicker startColor={workingCallout.startColor || defaultColor}
                         on:select={onSelectStartColor}
                         predefinedColors={calloutColors}/>
        </div>
        <div class="help-block">
            The primary colour of this callout.
            {#if !requireSecondColor}
                You can choose to
                <button class="btn btn-skinny"
                        on:click={() => requireSecondColor = true}>
                    add a second colour
                </button>
                which will appear on the lower half of the icon
            {/if}
        </div>

        {#if requireSecondColor}
            <div id="end-color">
                <ColorPicker startColor={workingCallout.endColor}
                             on:select={onSelectEndColor}
                             predefinedColors={calloutColors}/>
            </div>
            <div class="help-block">
                The secondary color of this callout, appears on the lower half of the icon.
                Switch to
                <button class="btn btn-skinny"
                        on:click={() => requireSecondColor = false}>
                    primary color only
                </button>
            </div>
        {/if}

        <button type="submit"
                class="btn btn-success"
                disabled={invalid}>
            Save
        </button>

        <button class="btn"
                on:click|preventDefault={cancel}>
            Cancel
        </button>
    </form>
{:else}
    <div>
        <Icon name="info-circle"/>
        Select a cell on the diagram to add a callout to or
        <button class="btn btn-skinny"
                on:click={cancel}>
            view existing callout list
        </button>
    </div>
{/if}