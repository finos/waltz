<script>
    import _ from "lodash";

    export let state;
    export let title = null;
    export let ctx = {};

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
        "main.data-type.view": {
            path: ctx => `data-types/${ctx.id}`,
            title: "DataType View"
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
    <a href={path}
       {title}>
        <slot></slot>
    </a>
{:else }
    <span>
        <slot/>
    </span>
{/if}