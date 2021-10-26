<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {appAlignments, diagramInfo, objects} from "../diagram-store";
    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";

    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectApplication, selectDiagramObject} from "../process-diagram-utils";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";

    let qry;

    function determineSymbol(objType) {
        switch (objType) {
            case 'Activity':
                return "square-o";
            case 'Event':
                return "circle-thin"
            default:
                throw 'Cannot find symbol for type: ' + objType
        }
    }

    $: objs = _
        .chain($objects)
        .filter(d => d.objectType === 'Activity' || d.objectType === 'Event')
        .map(d => Object.assign({
            id: d.objectId,
            name: d.name,
            kind: d.objectType,
            icon: determineSymbol(d.objectType),
            onSelect: () => selectDiagramObject(d)
        }))
        .uniqBy(d => d.id)
        .value();

    $: appInfo = _
        .chain($appAlignments)
        .map(a => Object.assign({
            id: a.applicationRef.id,
            name: a.applicationRef.name,
            kind:  entity.APPLICATION.name,
            icon: entity.APPLICATION.icon,
            onSelect: () => selectApplication(a.applicationRef)
        }))
        .uniqBy(a => a.id)
        .value();

    $: materials = _.orderBy(_.concat(objs, appInfo), d => d.name);

    $: listedMaterials = _.isEmpty(qry)
        ? materials
        : termSearch(materials, qry, ['name', 'kind']);

</script>

<h4>{$diagramInfo?.name}</h4>
{#if $diagramInfo?.description}
    <DescriptionFade text={$diagramInfo?.description}/>
    <br>
{/if}

<div class="help-block">
    <Icon name="info-circle"/>Select an Activity, Decision or Event on the diagram for more information, or use the
    search panel below.
</div>

{#if !_.isEmpty(materials)}
    <div>
        <SearchInput bind:value={qry}
                     placeholder="Search apps, activities and events..."/>

        <div class={_.size(listedMaterials) > 20 ? "waltz-scroll-region-300 scroll-materials" : "scroll-materials"}>
            <table class="table table-condensed table-hover small">
                <tbody>
                {#each listedMaterials as material}
                    <tr class="clickable"
                        on:click={() => material.onSelect()}>
                        <td width="5%" title={material.kind}><Icon name={material.icon}/></td>
                        <td width="95%">{material.name}</td>
                    </tr>
                {:else}
                    <tr>
                        <td>There are no listed materials to show</td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
{/if}

<style>
    .scroll-materials {
        margin-top: 1em;
        margin-bottom: 1em;
    }
</style>