<script>
    import {userStore} from "../../../../svelte-stores/user-store";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
    import ProposedFlowSection from "./ProposedFlowSection.svelte";
    import UserFlows from "./UserFlows.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {personStore} from "../../../../svelte-stores/person-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import ProposedFlows from "./ProposedFlows.svelte";

    $: userCall = userStore.load();
    $: user = $userCall?.data;

    $: personCall = personStore.getSelf();
    $: person = $personCall?.data;
    $: selectionOptions = {
        entityLifecycleStatuses: ["ACTIVE"],
        entityReference: {
            id: person ? person.id : null,
            kind: person ? person.kind : null
        },
        filters : {},
        scope: "CHILDREN"
    }

    $: logicalFlowCall = person ? logicalFlowStore.findBySelector(selectionOptions) : null;
    $: involvedFlows = $logicalFlowCall?.data ?
        $logicalFlowCall?.data
        : null;

    $: dataTypesCall = dataTypeStore.findAll();
    $: dataTypes = $dataTypesCall.data ?
        $dataTypesCall.data
        : [];

    $: dataTypeIdToNameMap = dataTypes.reduce((acc, d) => {
            acc[d.id] = d.name;
            return acc;
        }, {});
</script>

<div>
    { #if user }
        <PageHeader icon="shuffle"
        name="Data Flow Dashboard"
        small={user.userName}>
            <div slot="breadcrumbs">
                <ol class="waltz-breadcrumbs">
                    <li><ViewLink state="main">Home</ViewLink></li>
                    <li>Data Flow Dashboard</li>
                </ol>
            </div>
            <div slot="summary">
                <ProposedFlows userName={user.userName} dataTypeIdToNameMap={dataTypeIdToNameMap}/>
                <br/>
                <hr/>
                <UserFlows userName={user.userName} flows={involvedFlows}/>
                <br/>
                <hr/>
            </div>
        </PageHeader>
    { :else }
        <div></div>
    { /if }
</div>