<script>
    import {userStore} from "../../../../svelte-stores/user-store";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
    import ActionableFlows from "./ActionableFlows.svelte";
    import UserFlows from "./UserFlows.svelte";

    $: userCall = userStore.load();
    $: user = $userCall?.data;
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
                <ActionableFlows userName={user.userName}/>

                <h1>Involved Flows</h1>
                <UserFlows userName={user.userName}/>
            </div>
        </PageHeader>
    { :else }
        <div></div>
    { /if }
</div>