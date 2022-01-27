<script>

    import {reportGridKinds} from "./report-grid-utils";
    import _ from "lodash";
    import {userStore} from "../../../svelte-stores/user-store";
    import roles from "../../../user/system-roles";

    export let grid;
    export let doCancel = () => console.log("Cancel");
    export let doSave = () => console.log("Saving");

    let workingCopy = Object.assign({}, grid, {kind: grid?.kind || reportGridKinds.PRIVATE.key});

    $: userCall = userStore.load();
    $: user = $userCall.data;

    $: isGridAdmin = _.includes(user?.roles, roles.REPORT_GRID_ADMIN.key);

    function noChange(workingCopy) {
        return workingCopy?.name === grid?.name
            && workingCopy?.description === grid?.description
            && workingCopy?.kind === grid?.kind
    }

    function notSubmittable() {
        return _.isEmpty(workingCopy?.name)
            || _.isEmpty(workingCopy?.name.trim())
            || _.isNull(workingCopy?.kind);
    }

</script>


<form autocomplete="off">

    <div class="form-group">
        <label for="title">Title</label>
        <input class="form-control"
               id="title"
               placeholder="Grid Name"
               bind:value={workingCopy.name}>
    </div>

    <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control"
                  id="description"
                  bind:value={workingCopy.description}/>
    </div>


    <label for="kind">Kind</label>
    <div id="kind"
         class="form-group">
        <div class="radio">
            <label>
                <input type="radio"
                       style="display: block"
                       disabled={!isGridAdmin}
                       checked={workingCopy.kind === reportGridKinds.PRIVATE.key}
                       bind:group={workingCopy.kind}
                       value={reportGridKinds.PRIVATE.key}>
                {reportGridKinds.PRIVATE.name}
                <div class="help-block small">Private - (Recommended) - These grids can only be viewed if you are an owner or are subscribed to the group</div>
            </label>
            <br>
            <label>
                <input type="radio"
                       style="display: block;"
                       disabled={!isGridAdmin}
                       checked={workingCopy.kind === reportGridKinds.PUBLIC.key}
                       bind:group={workingCopy.kind}
                       value={reportGridKinds.PUBLIC.key}>
                {reportGridKinds.PUBLIC.name}
                <div class="help-block small">Public - These grids can be viewed by everyone, please contact an Admin to
                    set this grid to 'Public'
                </div>
            </label>
        </div>
    </div>

    <button type="submit"
            class="btn btn-success"
            disabled={notSubmittable() || noChange(workingCopy)}
            on:click|preventDefault={() => doSave(workingCopy)}>
        Save
    </button>

    <button class="btn btn-link"
            on:click={() => doCancel()}>
        Cancel
    </button>
</form>