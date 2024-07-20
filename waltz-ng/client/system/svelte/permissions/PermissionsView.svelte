<script>
import PageHeader from "../../../common/svelte/PageHeader.svelte";
import ViewLink from "../../../common/svelte/ViewLink.svelte";
import {onMount} from "svelte";
import {permissionViewStore} from "../../../svelte-stores/permission-view-store";
import {refToString} from "../../../common/entity-utils";
import EntityIcon from "../../../common/svelte/EntityIcon.svelte";
import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
import _ from "lodash";

let permissionsViewCall;
let selectedValues = null;

onMount(() => {
    permissionsViewCall = permissionViewStore.findAll();
});

$: raw = _
    .chain($permissionsViewCall?.data)
    .map((d, idx) => Object.assign({}, d, {id: idx}))
    .value();

$: grouped = _
    .chain(raw)
    .groupBy(d => `${d.subjectKind}#${d.qualifier ? refToString(d.qualifier) : "na"}#${d.parentKind}#${d.operation}`)
    .map((v, k) => ({k, v}))
    .orderBy(d => d.k)
    .value();
</script>

<PageHeader icon="users-rectangle"
            name="Permissions Groups">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.list">System Admin</ViewLink>
            </li>
            <li>Permission Group</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>Permissions groups are the basis for defining fine grained user permissions.
            </p>
        </div>
    </div>

    <div class="row waltz-sticky-wrapper">

        <!-- MAIN -->
        <div class="col-md-8">
            <table class="table table-condensed table-striped table-hover small">
                <thead>
                <tr>
                    <th>Subject</th>
                    <th>Qualifier</th>
                    <th>Primary Kind</th>
                    <th>Operation</th>
                </tr>
                </thead>
                <tbody>
                {#each grouped || [] as row}
                    {@const d = row.v[0]}
                    <tr on:click={() => selectedValues = row.v}
                        class="clickable">
                        <td><EntityIcon kind={d.subjectKind}
                                        showName="true"/></td>
                        <td>{#if d.qualifier}<EntityLabel ref={d.qualifier}/>{/if}</td>
                        <td><EntityIcon kind={d.parentKind} showName="true"/></td>
                        <td>{d.operation}</td>
                        <td></td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>

        <!-- CONTEXT -->
        <div class="col-md-4">
            {#if _.isNil(selectedValues)}
                <div class="waltz-sticky-part help-block">
                    <h4>Help</h4>
                    <dl>
                        <dt>Subject</dt>
                        <dd>The subject entity kind relates to the class of entity the permission applies to.  Subsequent columns refine this selection.</dd>
                        <dt>Qualifier</dt>
                        <dd>The optional qualifier refines the subject kind, for instance if the subject was a Viewpoint rating the qualifier would indicate which measurable category the permissions apply to.</dd>
                        <dt>Primary Kind</dt>
                        <dd>The entity kind the subject/qualifier applies to/  Used when a subject entity is owned but different kinds of entity, for example a flow can be owned by applications, actors and end user applications.</dd>
                        <dt>Operation</dt>
                        <dd>The type of operation being performed, one of:
                            <ul>
                                <li>ADD - creating new entries</li>
                                <li>REMOVE - removing existing entries</li>
                                <li>UPDATE - updating existing entries</li>
                                <li>ATTEST - attesting entries, only applicable for Flows and Viewpoint Ratings</li>
                            </ul>
                        </dd>
                    </dl>
                    <hr>
                    Once you select a row you this panel will be replaced with a detail view.
                    This view shows the permission groups, which contain involvement groups, which then contain specific involvement kinds.
                    This sum list of involvement kinds show which users (via their involvements) can perform the operation on the specified set of entities.
                </div>
            {:else}
                {@const d = selectedValues[0]}
                {@const permissionGroups = _.chain(selectedValues).map(d => d.permissionGroup).uniqBy(d => d.id).value()}

                <div class="waltz-sticky-part">
                    <h4>Overview</h4>

                    <table class="table table-condensed small table-striped">
                        <tbody>
                            <tr>
                                <th>Subject</th>
                                <td><EntityIcon kind={d.subjectKind}
                                                showName="true"/></td>
                            </tr>
                            <tr>
                                <th>Qualifier</th>
                                <td>
                                    {#if d.qualifier}
                                        <EntityLabel ref={d.qualifier}/> (id: {d.qualifier.id})
                                    {:else}
                                        -
                                    {/if}
                                </td>
                            </tr>
                            <tr>
                                <th>Parent Kind</th>
                                <td><EntityIcon kind={d.parentKind}
                                                showName="true"/></td>
                            </tr>
                            <tr>
                                <th>Operation</th>
                                <td>{d.operation}</td>
                            </tr>
                        </tbody>
                    </table>

                    <h4>Permissions</h4>

                    <table class="table table-condensed table-striped small">
                        <thead>
                            <tr>
                                <th>Permission Group</th>
                                <th>Involvement Group</th>
                                <th>Involvement Kind</th>
                            </tr>
                        </thead>
                        <tbody>
                            {#each permissionGroups as pg}
                                {@const involvementGroups = _.chain(selectedValues).filter(d => d.permissionGroup.id === pg.id).map(d => d.involvementGroup).uniqBy(d => d.id).value()}
                                {#each involvementGroups as ig}
                                    {@const involvementKinds = _.chain(selectedValues).filter(d => d.permissionGroup.id === pg.id).filter(d => d.involvementGroup.id === ig.id).map(d => d.involvementKind).uniqBy(d => d.id).value()}
                                    {#each involvementKinds as ik}
                                        <tr>
                                            <td>{pg.name}</td>
                                            <td>{ig.name}</td>
                                            <td>{ik.name}</td>
                                        </tr>
                                    {/each}
                                {/each}
                            {/each}
                        </tbody>
                    </table>

                    <button class="btn btn-skinny"
                            on:click={() => selectedValues = null}>
                        Close
                    </button>
                </div>
            {/if}

        </div>
    </div>
</div>

<style>
    dd {
        padding-bottom: 0.6em;
    }
</style>
