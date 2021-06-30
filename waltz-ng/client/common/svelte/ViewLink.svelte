<script>
    import _ from "lodash";

    export let state;
    export let title = null;
    export let ctx = {};
    export let isSecondaryLink = false;

    let path;

    const routes = {
        "main": {
            path: () => "",
            title: "Home page"
        },
        "main.system.list": {
            path: () => "system/list",
            title: "System Administration"
        },
        "main.actor.view": {
            path: ctx => `actor/${ctx.id}`,
            title: "Actor View"
        },
        "main.app.view": {
            path: ctx => `application/${ctx.id}`,
            title: "Application View"
        },
        "main.app-group.view": {
            path: ctx => `app-group/${ctx.id}`,
            title: "Application Group View"
        },
        "main.change-initiative.view": {
            path: ctx => `change-initiative/${ctx.id}`,
            title: "Change Initiative View"
        },
        "main.data-type.view": {
            path: ctx => `data-types/${ctx.id}`,
            title: "DataType View"
        },
        "main.flow-diagram.view": {
            path: ctx => `flow-diagram/${ctx.id}`,
            title: "Flow Diagram View"
        },
        "main.logical-flow.view": {
            path: ctx => `logical-flow/${ctx.id}`,
            title: "Logical Flow View"
        },
        "main.measurable.view": {
            path: ctx => `measurable/${ctx.id}`,
            title: "Measurable View"
        },
        "main.org-unit.view": {
            path: ctx => `org-units/${ctx.id}`,
            title: "Org Unit View"
        },
        "main.server.view": {
            path: ctx => `server/${ctx.id}`,
            title: "Server View"
        }
    };

    $: {
        path = _.get(routes, [state, "path"], () => "")(ctx);
        title = title || _.get(routes, [state, "title"], state);
    }

</script>

{#if path}
    {#if !isSecondaryLink}
        <a href={path}
           {title}>
            <slot></slot>
        </a>
    {:else}
        <a class="secondary-link"
           href={path}
           {title}>
            <slot></slot>
        </a>
    {/if}
{:else }
    <span>
        <slot/>
    </span>
{/if}


<style type="text/scss">
    @import '../../../style/variables';

    .secondary-link {
        color: $waltz-font-color;
        font-weight: bold;

      &:hover {
        color: $waltz-link-color;
      }
    }
</style>