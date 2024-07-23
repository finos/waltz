<script>
    import Icon from "../../common/svelte/Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import RoleList from "./RoleList.svelte";

    const dispatch = createEventDispatcher();

    export let roles;

</script>


<div class="row">
    <div class="col-md-9">
        <RoleList {roles}/>
    </div>
    <div class="col-md-3 small">
        <div class="help-block">
            Roles are used to grant coarse grained permissions to users.

            <br>
            <br>

            Coarse grained permissions are those that are not data driven -
            you can't use them to restrict permissions on apps to only users with a certain involvement
            (if you want to be able to do this look at the fine grained permission support).

            <br>
            <br>

            Waltz ships with a set of pre-defined roles, these are indicated with the lock icon (<Icon name="lock"/>)
            and should <em>not</em> be removed.

            <br>
            <br>

            Select a role on the table to see more information about it.


        </div>
    </div>
</div>