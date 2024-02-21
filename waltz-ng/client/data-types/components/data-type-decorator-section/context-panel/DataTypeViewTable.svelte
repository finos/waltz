<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let dataType;

</script>


<table class="table table-condensed small">
    <colgroup>
        <col width="30%">
        <col width="70%">
    </colgroup>
    <tbody>
    <tr>
        <td>Name</td>
        <td>
            <EntityLink ref={dataType}/>
        </td>
    </tr>
    <tr>
        <td>Code</td>
        <td>
            {dataType.code}/>
        </td>
    </tr>
    <tr>
        <td>Description</td>
        <td>
            <DescriptionFade text={dataType.description}/>
        </td>
    </tr>
    {#if dataType.deprecated}
        <tr>
            <td>Deprecated</td>
            <td>
                <div>
                    <span style="color: darkred">
                        <Icon name="exclamation-triangle"/>
                    </span>
                    This data type has been deprecated
                </div>
            </td>
        </tr>
    {/if}
    </tbody>
</table>

<style>

    table {
        margin-bottom: 1em !important;
    }

</style>