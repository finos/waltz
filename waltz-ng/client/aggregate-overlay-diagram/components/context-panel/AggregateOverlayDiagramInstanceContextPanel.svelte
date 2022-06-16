<script>

    import CalloutList from "../aggregate-overlay-diagram/callout/CalloutList.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import ImageDownloadLink from "../../../common/svelte/ImageDownloadLink.svelte";

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let svgDetail = getContext("svgDetail");

</script>

<div>
    <h4>
        {$selectedInstance?.name}
    </h4>
    <table class="table table-condensed small">
        <colgroup>
            <col width="50%">
            <col width="50%">
        </colgroup>
        <tbody>
        <tr>
            <td>Diagram</td>
            <td>{$selectedDiagram?.name}</td>
        </tr>
        <tr>
            <td>Vantage Point</td>
            <td>
                <EntityLink ref={$selectedInstance?.parentEntityReference}/>
            </td>
        </tr>
        <tr>
            <td>Last Updated</td>
            <td>
                <LastEdited entity={$selectedInstance}/>
            </td>
        </tr>
        <tr>
            <td>Description</td>
            <td>
                <DescriptionFade text={$selectedInstance?.description}/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div>
    <CalloutList/>
</div>
<div>
    <hr>
    <ImageDownloadLink styling="link"
                       element={$svgDetail}
                       filename={`${$selectedInstance?.name}-image.png`}/>
</div>