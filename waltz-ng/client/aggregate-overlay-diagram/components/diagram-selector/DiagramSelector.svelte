<script>

    import {createEventDispatcher} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {entity} from "../../../common/services/enums/entity";

    export let diagrams = [];

    const dispatch = createEventDispatcher();


    function selectDiagram(diagram) {
        dispatch("select", diagram)
    }

</script>

{#if !_.isEmpty(diagrams)}
    <h4>Select a diagram</h4>
    <p>
        <Icon name="info-circle"/>
        Select a diagram from the list below to see a dynamic overlay of waltz data and any instances of this diagram
        which have been created.
    </p>

    <table class="table table-condensed table-hover">
        <colgroup>
            <col width="30%"/>
            <col width="30%"/>
            <col width="40%"/>
        </colgroup>
        <thead>
        <tr>
            <th>Diagram</th>
            <th>Aggregated Kind</th>
            <th>Last Updated</th>
        </tr>
        </thead>
        <tbody>
        {#each diagrams as diagram}
            <tr class="clickable"
                on:click={() => selectDiagram(diagram)}>
                <td>
                    {diagram.name}
                </td>
                <td>
                    <Icon name={entity[diagram.aggregatedEntityKind].icon}/>
                    {entity[diagram.aggregatedEntityKind].name}
                </td>
                <td>
                    <span class="text-muted">
                    <LastEdited entity={diagram}/>
                </span>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}

