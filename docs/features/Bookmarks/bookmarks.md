# Bookmarks

Most entities in Waltz can have bookmarks associated with them.  Bookmarks allow for a URL to be formally associated with the entity. In addition to the basic URL a bookmark may contain:

- title
- description
- kind 
- primary flag

The `title` should describe the link, e.g. '_Confluence wiki docs_' and the `description` can optionally describe the link in more detail.

The `kind` is used to classify the links and typical entries may include:

- Documentation (e.g. wiki link)
- Source Code Management (e.g. GitHub link)
- Issue Tracking (e.g. Jira link)
- Monitoring (e.g. Geneos link)
- Application Instance (e.g. prod/Dev/uat links)

As with other soft coded enumerations this list of bookmark kinds can be configured in the `enum_value` table.

By using bookmark kinds to categorize types of bookmarks it becomes possible to craft data quality rules sure as 'all applications classified as `in-house` _must_ have a source code control bookmark defined. These rules must currently be developed as an external job.

