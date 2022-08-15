<script>

    import {entity} from "../../../../common/services/enums/entity";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntityPicker from "../pickers/EntityPicker.svelte";

    export let onSelect = (d) => console.log("Selecting entity", d);
    export let onDeselect = (d) => console.log("Deselecting entity", d);
    export let selectionFilter = () => true;
    export let subjectKind;

    let selectedEntityKind = null;
    let showDropdown = false;

    let baseKinds = [
        entity.INVOLVEMENT_KIND,
        entity.SURVEY_QUESTION,
        entity.ASSESSMENT_DEFINITION,
        entity.APP_GROUP,
        entity.SURVEY_INSTANCE,
    ];

    $: entityKinds = entityKindsBySubjectKind[subjectKind] || baseKinds;

    const entityKindsBySubjectKind = {
        "APPLICATION": _.orderBy(_.concat(baseKinds, [entity.ATTESTATION, entity.APPLICATION, entity.DATA_TYPE, entity.MEASURABLE, entity.COST_KIND]), d => d.name),
        "CHANGE_INITIATIVE": _.orderBy(_.concat(baseKinds, [entity.CHANGE_INITIATIVE]), d => d.name)
    };

    function toggleDropdown() {
        showDropdown = !showDropdown
    }

    function selectEntityKind(entityKind) {
        selectedEntityKind = entityKind;
        showDropdown = false;
    }

    function cancel() {
        selectedEntityKind = null;
        showDropdown = false
    }

</script>


<div class="row">
    <div class="col-sm-12">
        <div class="btn-group"
             style="width: 100%; outline: 1px solid #cccccc;">
            <div class:expanded={showDropdown}>
                <button on:click={() => toggleDropdown()}
                        class="btn btn-skinny">
                    {#if _.isNull(selectedEntityKind)}
                        <span>
                            Select an entity kind
                        </span>
                    {:else}
                        <span>
                            {selectedEntityKind.name}
                        </span>
                    {/if}
                    <span class="pull-right">
                        {#if showDropdown}
                            <Icon name="caret-up"/>
                        {:else}
                            <Icon name="caret-down"/>
                        {/if}
                    </span>
                </button>
            </div>
            <div>
                {#if showDropdown}
                    <ul>
                        {#if selectedEntityKind}
                            <li>
                                <div class="text-muted clickable"
                                     on:click={() => selectEntityKind(null)}>
                                    Select an entity kind
                                </div>
                            </li>
                        {/if}
                        {#each entityKinds as entityKind}
                            <li>
                                <button class="btn btn-skinny"
                                        on:click={() => selectEntityKind(entityKind)}>
                                    <Icon name={entityKind.icon}/> {entityKind.name}
                                </button>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-sm-12">
        {#if selectedEntityKind}
            <EntityPicker {onSelect}
                          {onDeselect}
                          {selectionFilter}
                          entityKind={selectedEntityKind?.key}/>
        {:else}
            <div class="help-block small">
                <Icon name="info-circle"/>Use the picker to select an entity kind
            </div>
        {/if}
    </div>
</div>

{#if selectedEntityKind}
    <button class="btn btn-skinny"
        on:click={() => cancel()}>
        <Icon name="times"/>Close
    </button>
{/if}

<style type="text/scss">
    ul {
        padding: 0;
        margin: 0;
        outline: 1px solid #cccccc;
        list-style: none;
    }

    li {
        padding-top: 0;

        &:hover {
            background: #f3f9ff;
         }

        div {
            width: 100%;
            padding: 0.5em;
            text-align: left;
        }
    }

    button {
        width: 100%;
        padding: 0.5em;
        text-align: left;
    }

    .expanded {
        background: #eee;
    }
</style>