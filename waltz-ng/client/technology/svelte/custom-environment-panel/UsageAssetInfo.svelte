<script>
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let asset;

</script>


{#if _.isNil(asset)}
    <div style="padding-top: 0.2em;">
        <span>
            <Icon name="info-circle"/>
            Select an asset on the tree for more info
        </span>
    </div>
{:else if asset.usage.entityReference.kind === 'SERVER_USAGE'}
    <div>
        <h4><EntityLink ref={asset.asset}/></h4>
        <table class="table table-condensed small">
            <tbody>
            <tr>
                <td>External Id</td>
                <td>{asset.asset.externalId || '-'}</td>
            </tr>
            <tr>
                <td>OS</td>
                <td>{asset.asset.operatingSystem}</td>
            </tr>
            <tr>
                <td>OS Verstion</td>
                <td>{asset.asset.operatingSystemVersion}</td>
            </tr>
            <tr>
                <td>Country</td>
                <td>{asset.asset.country}</td>
            </tr>
            <tr>
                <td>Location</td>
                <td>{asset.asset.location}</td>
            </tr>
            <tr>
                <td>OS EOL</td>
                <td>{asset.asset.operatingSystemEndOfLifeDate || '-'}</td>
            </tr>
            <tr>
                <td>HW EOL</td>
                <td>{asset.asset.hardwareEndOfLifeDate || '-'}</td>
            </tr>
            <tr>
                <td>Virtual</td>
                <td>{asset.asset.virtual ? 'Yes' : 'No' }</td>
            </tr>
            </tbody>
        </table>
    </div>
{:else if asset.usage.entityReference.kind === 'DATABASE'}
    <div>
        <h4><EntityLink ref={asset.asset}/></h4>
        <table class="table table-condensed small">
            <tbody>
            <tr>
                <td>External Id</td>
                <td>{asset.asset.externalId || '-'}</td>
            </tr>
            <tr>
                <td>Instance Name</td>
                <td>{asset.asset.instanceName}</td>
            </tr>
            <tr>
                <td>Vendor</td>
                <td>{asset.asset.dbmsVendor}</td>
            </tr>
            <tr>
                <td>Dbms Name</td>
                <td>{asset.asset.dbmsName}</td>
            </tr>
            <tr>
                <td>Dbms Version</td>
                <td>{asset.asset.dbmsVersion}</td>
            </tr>
            <tr>
                <td>EOL</td>
                <td>{asset.asset.endOfLifeDate}</td>
            </tr>
            </tbody>
        </table>
    </div>
{/if}