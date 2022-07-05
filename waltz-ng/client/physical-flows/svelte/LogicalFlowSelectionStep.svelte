<script>
    import {expandedSections, logicalFlow} from "./physical-flow-editor-store";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import _ from "lodash";
    import RouteSelector from "./RouteSelector.svelte";
    import LogicalFlowLabel from "./LogicalFlowLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import StepHeader from "./StepHeader.svelte";
    import {determineExpandedSections, sections} from "./physical-flow-registration-utils";
    import EntitySearchSelector from "../../common/svelte/EntitySearchSelector.svelte";
    import {toEntityRef} from "../../common/entity-utils";
    import toasts from "../../svelte-stores/toast-store";


    export let primaryEntityRef;

    let logicalFlowsCall = null;


    const Modes = {
        CREATE: "CREATE",
        LIST: "LIST"
    }

    let source;
    let target;

    let activeMode = Modes.LIST;

    $: {
        if (primaryEntityRef) {
            logicalFlowsCall = logicalFlowStore.findByEntityReference(primaryEntityRef);
        }
    }

    $: logicalFlows = _
        .chain($logicalFlowsCall?.data)
        .orderBy([
            d => d.source.name.toLowerCase(),
            d => d.target.name.toLowerCase()
        ])
        .value();

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.ROUTE);
    }

    function createNewLogical() {
        const command = {
            source,
            target
        }
        logicalFlowStore.addFlow(command)
            .then(r => {
                toasts.success(`Successfully created new logical flow from ${source.name} to ${target.name}`);
                selectFlow(r.data);
            });
    }

    function onSelectSource(sourceEntity) {
        source = sourceEntity;
    }

    function onSelectTarget(targetEntity) {
        target = targetEntity;
    }

    function cancel() {
        source = null;
        target = null;
        activeMode = Modes.LIST
    }

    function selectFlow(flow) {
        $logicalFlow = flow;
        const specSectionOpen = _.includes($expandedSections, sections.SPECIFICATION);
        if (!specSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.SPECIFICATION)
        }
    }

    $: expanded = _.includes($expandedSections, sections.ROUTE);

</script>

<StepHeader label="Route"
            icon="map-o"
            checked={$logicalFlow !== null}
            {expanded}
            onToggleExpanded={toggleSection}/>

{#if expanded}
    <div class="step-body">
        {#if !$logicalFlow}
            {#if activeMode === Modes.LIST}
                <div class="help-block">
                    <Icon name="info-circle"/>
                    Select which nodes this physical flow is between.
                    <br>
                    If the route is not listed you can
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.CREATE}>
                        create a new logical flow
                    </button>
                </div>

                <RouteSelector node={primaryEntityRef}
                               flows={logicalFlows}
                               on:select={(evt) => selectFlow(evt.detail)}/>

            {:else if activeMode === Modes.CREATE}

                <div class="help-block">
                    <Icon name="info-circle"/>
                    Pick a source and target for the new flow
                </div>

                <form on:submit|preventDefault={createNewLogical}>
                    <div class="form-group">
                        <label for="source">
                            Source
                        </label>
                        <div id="source">
                            {#if source}
                                <div>
                                    {source.name}
                                    <button class="btn btn-skinny"
                                            on:click={() => source = null}>
                                        <Icon name="times"/>
                                        select a different source
                                    </button>
                                </div>
                            {:else}
                                <button class="btn btn-info btn-sm"
                                        on:click={() => onSelectSource(toEntityRef(primaryEntityRef))}>
                                    {primaryEntityRef.name}
                                </button>
                                <EntitySearchSelector on:select={(evt) => onSelectSource(evt.detail)}
                                                      placeholder="... or search for source"
                                                      entityKinds={['APPLICATION', 'ACTOR']}>
                                </EntitySearchSelector>
                            {/if}
                        </div>
                    </div>
                    <div class="help-block">
                        Source of this data flow
                    </div>

                    <div class="form-group">
                        <label for="target">
                            Target
                        </label>
                        <div id="target">
                            {#if target}
                                <div>
                                    {target.name}
                                    <button class="btn btn-skinny"
                                            on:click={() => target = null}>
                                        <Icon name="times"/>
                                        select a different target
                                    </button>
                                </div>
                            {:else}
                                <button class="btn btn-info btn-sm"
                                        on:click={() => onSelectTarget(toEntityRef(primaryEntityRef))}>
                                    {primaryEntityRef.name}
                                </button>
                                <EntitySearchSelector on:select={(evt) => onSelectTarget(evt.detail)}
                                                      placeholder="... or search for target"
                                                      entityKinds={['APPLICATION', 'ACTOR']}>
                                </EntitySearchSelector>
                            {/if}
                        </div>
                    </div>
                    <div class="help-block">
                        Target of this data flow
                    </div>


                    <button class="btn btn-success"
                            disabled={!(source && target)}
                            on:click={() => createNewLogical()}>
                        Save
                    </button>
                    <button class="btn btn-skinny"
                            on:click={() => cancel()}>
                        Cancel
                    </button>
                </form>
            {/if}

        {:else}

            <div>
                <span style="font-weight: lighter">
                    Selected Route:</span>
                <LogicalFlowLabel logicalFlow={$logicalFlow}
                                  {primaryEntityRef}/>
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => $logicalFlow = null}>
                <Icon name="times"/>
                Pick a different route
            </button>

        {/if}
    </div>
{/if}


<style>
    .step-body {
        padding-left: 1em;
    }
</style>