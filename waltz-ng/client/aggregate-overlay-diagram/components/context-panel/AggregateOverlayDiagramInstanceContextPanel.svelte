<script>

    import CalloutList from "../aggregate-overlay-diagram/CalloutList.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");

</script>

<div>
    <h4>
        {$selectedInstance?.name}
    </h4>
    <table class="table table-condensed small">
        <tbody>
        <tr>
            <td width="50%">Diagram</td>
            <td width="50%">{$selectedDiagram?.name}</td>
        </tr>
        <tr>
            <td width="50%">Vantage Point</td>
            <td width="50%">
                <EntityLink ref={$selectedInstance?.parentEntityReference}/>
            </td>
        </tr>
        <tr>
            <td width="50%">Last Updated</td>
            <td width="50%">
                <LastEdited entity={$selectedInstance}/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div>
    <CalloutList/>
</div>