<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";
    import PhysicalSpecificationSelector from "./PhysicalSpecificationSelector.svelte";
    import {physicalSpecStore} from "../../svelte-stores/physical-spec-store";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import {expandedSections, physicalSpecification, nestedEnums} from "./physical-flow-editor-store";
    import {
        determineExpandedSections,
        sections,
        toDataFormatKindName,
        toOptions
    } from "./physical-flow-registration-utils";
    import Icon from "../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";
    import toasts from "../../svelte-stores/toast-store";

    const Modes = {
        EXISTING: "EXISTING",
        CREATE: "CREATE"
    }

    export let primaryEntityRef;

    let workingCopy = {};
    let specificationCall;
    let activeMode = Modes.CREATE;
    let enumsCall = enumValueStore.load();

    onMount(() => {
        if ($physicalSpecification) {
            workingCopy = Object.assign({}, $physicalSpecification);
        } else {
            workingCopy = {
                externalId: null,
                description: "",
                format: null,
                name: ""
            }
        }
    })

    function save() {
        $physicalSpecification = workingCopy;
        openNextSection();
    }

    function selectSpecification(evt) {
        $physicalSpecification = evt.detail;
        workingCopy = evt.detail;
        openNextSection();
    }

    function openNextSection() {
        const flowSectionOpen = _.includes($expandedSections, sections.FLOW);
        if (!flowSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.FLOW)
        }
    }

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.SPECIFICATION);
    }

    function editSpec() {
        if ($physicalSpecification.id) {
            workingCopy.id = null;
            toasts.info("Instead of altering the original specification, a new one will be created for this flow")
        }
        $physicalSpecification = null;
        activeMode = Modes.CREATE;
    }

    $: {
        if (primaryEntityRef) {
            const selector = mkSelectionOptions(primaryEntityRef);
            specificationCall = physicalSpecStore.findBySelector(selector)
        }
    }

    $: specifications = $specificationCall?.data;
    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);
    $: dataFormatKinds = toOptions(enumsByType, "DataFormatKind");

    $: done = workingCopy.name
        && workingCopy.format

    $: expanded = _.includes($expandedSections, sections.SPECIFICATION);

</script>

<StepHeader label="Specification"
            icon="file-code-o"
            checked={$physicalSpecification}
            {expanded}
            onToggleExpanded={toggleSection}/>

{#if expanded}
    <div class="step-body">

        {#if $physicalSpecification}

            <div>
                <div style="font-weight: lighter">Selected Specification: {$physicalSpecification.name}</div>
                <ul>
                    <li>
                        Format: {toDataFormatKindName($nestedEnums, $physicalSpecification.format)}
                    </li>
                </ul>
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => editSpec()}>
                <Icon name="times"/>
                Pick a different specification
            </button>

        {:else}

            {#if activeMode === Modes.EXISTING}

                <PhysicalSpecificationSelector {specifications}
                                               on:select={selectSpecification}/>
                <br>
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.CREATE}>
                    Create new specification
                </button>

            {:else if activeMode === Modes.CREATE}

                <div class="help-block">
                    <Icon name="info-circle"/>
                    Create a new specification
                    {#if !_.isEmpty(specifications)}
                        <span> or select from
                            <button class="btn btn-skinny"
                                    on:click={() => activeMode = Modes.EXISTING}>
                                existing specifications
                            </button>
                        </span>
                    {:else}
                        , once created, the specification can be used across multiple flows
                    {/if}
                </div>

                <form on:submit|preventDefault={save}>

                    <div class="form-group">
                        <label for="name">
                            Name
                            <small class="text-muted">(required)</small>
                        </label>

                        <input class="form-control"
                               autocomplete="waltz-no-autocomplete"
                               type="text"
                               id="name"
                               bind:value={workingCopy.name}/>
                    </div>
                    <div class="help-block">
                        Name of this specification
                    </div>

                    <EnumSelect options={dataFormatKinds}
                                bind:value={workingCopy.format}
                                mandatory="true"
                                name="Format">
                        <div slot="help">
                            Describes the structure of the data being transferred
                        </div>
                    </EnumSelect>

                    <div class="form-group">
                        <label for="description">
                            Description
                        </label>
                        <textarea class="form-control"
                                  id="description"
                                  rows="2"
                                  bind:value={workingCopy.description}/>
                    </div>
                    <div class="help-block">Description of the specification</div>

                    <div class="form-group">
                        <label for="external-id">
                            External Id
                        </label>
                        <input class="form-control"
                               id="external-id"
                               bind:value={workingCopy.externalId}/>
                    </div>
                    <div class="help-block">
                        External identifier for this specification
                    </div>

                    <button class="btn btn-skinny"
                            disabled={!done}
                            on:click={() => save()}>
                        Done
                    </button>
                </form>
            {/if}
        {/if}
    </div>
{/if}

<style>
    .step-body {
        padding-left: 1em;
    }
</style>