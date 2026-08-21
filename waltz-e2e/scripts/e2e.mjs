/**
 * Ephemeral e2e runner.
 *
 * Stands up a throwaway Waltz backend entirely in containers — nothing on the host is assumed
 * beyond Docker + this repo's build artifacts — then runs the Playwright suite against it and
 * tears everything down.
 *
 *   DB (container) ── Waltz app (container, liquibase + Tomcat) ── LoadAll (sample data)
 *                                  │
 *                     Playwright ──┘  (WALTZ_BASE_URL = app's mapped port)
 *
 * Config via env:
 *   E2E_DB_TARGET     postgres | mssql            (default: postgres)
 *   E2E_WALTZ_IMAGE   Waltz app image            (default: per-target dev image)
 *   E2E_JOBS_JAR      path to uber jobs jar      (default: ../waltz-jobs/target/uber-waltz-jobs-*.jar)
 *   E2E_SKIP_LOADALL  set to "1" to skip seeding (faster for specs that self-seed)
 *
 * NB: the app image and jobs jar must match the DB target (the MSSQL artefacts are built with the
 * jOOQ Pro SQLSERVER dialect).
 *
 * Any extra args are passed through to `playwright test`, e.g.:
 *   npm run test:e2e -- tests/bookmarks.spec.js
 */
import { GenericContainer, Network, Wait } from "testcontainers";
import { spawnSync } from "node:child_process";
import { existsSync, readdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const e2eDir = resolve(__dirname, "..");
const repoRoot = resolve(e2eDir, "..");

const APP_PORT = 8080;
const DB_TARGET = (process.env.E2E_DB_TARGET || "postgres").toLowerCase();

// Per-DB-target configuration: the DB container, how the app connects, and how LoadAll connects.
const DB_TARGETS = {
    postgres: {
        defaultImage: "waltz-dev-waltz:latest",
        db: {
            image: "postgres:16",
            alias: "postgres",
            env: { POSTGRES_DB: "waltz", POSTGRES_USER: "waltz", POSTGRES_PASSWORD: "waltz" },
            createDbCmd: null
        },
        appEnv: {
            DB_HOST: "postgres", DB_PORT: "5432", DB_NAME: "waltz",
            DB_USER: "waltz", DB_PASSWORD: "waltz", DB_SCHEME: "public"
        },
        loadAllProps: {
            "database.url": "jdbc:postgresql://postgres:5432/waltz",
            "database.user": "waltz",
            "database.password": "waltz",
            "database.schema": "public",
            "database.driver": "org.postgresql.Driver",
            "jooq.dialect": "POSTGRES"
        }
    },
    mssql: {
        defaultImage: "waltz-mssql:latest",
        db: {
            image: "hmxlabs/mssql-fts:2022-latest",
            alias: "mssql",
            env: { ACCEPT_EULA: "Y", SA_PASSWORD: "Waltz#123", MSSQL_PID: "Express" },
            // SQL Server does not auto-create the app database; create it once it accepts connections.
            createDbCmd: [
                "/bin/bash", "-lc",
                "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'Waltz#123' -C " +
                "-Q \"IF DB_ID('waltz') IS NULL CREATE DATABASE [waltz];\" || " +
                "/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Waltz#123' " +
                "-Q \"IF DB_ID('waltz') IS NULL CREATE DATABASE [waltz];\""
            ]
        },
        appEnv: {
            DB_HOST: "mssql", DB_PORT: "1433", DB_NAME: "waltz",
            DB_USER: "sa", DB_PASSWORD: "Waltz#123", DB_SCHEME: "dbo"
        },
        loadAllProps: {
            "database.url": "jdbc:sqlserver://mssql:1433;databaseName=waltz;encrypt=true;trustServerCertificate=true",
            "database.user": "sa",
            "database.password": "Waltz#123",
            "database.schema": "dbo",
            "database.driver": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jooq.dialect": "SQLSERVER2014"
        }
    }
};

const target = DB_TARGETS[DB_TARGET];
if (!target) {
    console.error(`Unknown E2E_DB_TARGET '${DB_TARGET}' (expected: postgres | mssql)`);
    process.exit(1);
}

const IMAGE = process.env.E2E_WALTZ_IMAGE || target.defaultImage;

function log(msg) {
    console.log(`\x1b[36m[e2e]\x1b[0m ${msg}`);
}

function resolveJobsJar() {
    if (process.env.E2E_JOBS_JAR) return process.env.E2E_JOBS_JAR;
    const targetDir = join(repoRoot, "waltz-jobs", "target");
    const jar = existsSync(targetDir)
        ? readdirSync(targetDir).find(f => /^uber-waltz-jobs-.*\.jar$/.test(f))
        : undefined;
    if (!jar) {
        throw new Error(
            "Could not find waltz-jobs/target/uber-waltz-jobs-*.jar. Build it first " +
            "(e.g. `mvn -pl waltz-jobs -am package -P waltz-postgres,dev-postgres -DskipTests`) " +
            "or set E2E_JOBS_JAR."
        );
    }
    return join(targetDir, jar);
}

const loadAllProps = Object.entries(target.loadAllProps)
    .map(([k, v]) => `${k}=${v}`)
    .concat(["database.pool.max=8", "waltz.base.url=http://localhost:8080/"])
    .join("\n") + "\n";

// Console-only logging for LoadAll — avoids the app image's file appenders (which target a
// ../logs dir that may not exist in the exec's working dir and spew errors).
const loadAllLogback =
`<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder><pattern>%d{HH:mm:ss} %-5level %logger{20} - %msg%n</pattern></encoder>
    </appender>
    <root level="INFO"><appender-ref ref="STDOUT"/></root>
</configuration>
`;

// Roles the specs' write flows need. Granting them to `admin` ONCE up front (serially, before the
// parallel suite runs) removes the lost-update race that otherwise occurs when multiple specs
// read-modify-write the shared admin user's roles concurrently.
const ADMIN_ROLES = [
    "ADMIN", "ACTOR_ADMIN", "AGGREGATE_OVERLAY_DIAGRAM_EDITOR", "APP_EDITOR",
    "ASSESSMENT_DEFINITION_ADMIN", "ATTESTATION_ADMIN", "AUTHORITATIVE_SOURCE_EDITOR",
    "BETA_TESTER", "BULK_FLOW_EDITOR", "BULK_LEGAL_ENTITY_RELATIONSHIP_EDITOR", "BOOKMARK_EDITOR",
    "CAPABILITY_EDITOR", "CHANGE_INITIATIVE_EDITOR", "CHANGE_SET_EDITOR", "EUDA_ADMIN",
    "INVOLVEMENT_EDITOR", "LINEAGE_EDITOR", "LOGICAL_DATA_FLOW_EDITOR", "ORG_UNIT_EDITOR",
    "PHYSICAL_SPECIFICATION_EDITOR", "RATING_EDITOR", "RATING_SCHEME_ADMIN", "REPORT_GRID_ADMIN",
    "SCENARIO_ADMIN", "SCENARIO_EDITOR", "SURVEY_ADMIN", "SURVEY_TEMPLATE_ADMIN", "TAXONOMY_EDITOR",
    "USER_ADMIN"
];

async function pollHttp(url, timeoutMs) {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
        try {
            const res = await fetch(url);
            if (res.status === 200) return;
        } catch {
            // not up yet
        }
        await new Promise(r => setTimeout(r, 2000));
    }
    throw new Error(`Timed out waiting for ${url}`);
}

// SQL Server accepts TCP before it accepts logins; retry the create-database command until it works.
async function ensureDatabase(dbContainer, cmd, timeoutMs) {
    const deadline = Date.now() + timeoutMs;
    let last;
    while (Date.now() < deadline) {
        const res = await dbContainer.exec(cmd);
        if (res.exitCode === 0) return;
        last = res.output;
        await new Promise(r => setTimeout(r, 3000));
    }
    throw new Error(`Could not create database within timeout. Last output:\n${last}`);
}

// Prime the freshly-started (cold) JVM so the first real test interactions aren't pathologically
// slow, and grant admin the full role set. Hits the heavier reference endpoints the UI pages
// depend on; failures are ignored.
async function warmUp(baseURL) {
    log("warming up the backend…");
    let token;
    try {
        const res = await fetch(`${baseURL}/authentication/login`, {
            method: "POST",
            headers: { "content-type": "application/json" },
            body: JSON.stringify({ userName: "admin", password: "password" })
        });
        token = (await res.json()).token;
    } catch {
        return; // best-effort
    }

    try {
        log("granting admin the full role set (once, serially)…");
        await fetch(`${baseURL}/api/user/admin/roles`, {
            method: "POST",
            headers: { authorization: `Bearer ${token}`, "content-type": "application/json" },
            body: JSON.stringify({ roles: ADMIN_ROLES, comment: "e2e: seed admin roles" })
        });
    } catch {
        // best-effort; specs still grant their own roles as a fallback
    }

    const paths = [
        "/api/data-types",
        "/api/measurable-category/all",
        "/api/measurable/all",
        "/api/involvement-kind",
        "/api/assessment-definition",
        "/api/org-unit",
        "/api/rating-scheme",
        "/api/flow-classification",
        "/api/app/all",
        "/api/role"
    ];
    const headers = { authorization: `Bearer ${token}` };
    for (let pass = 0; pass < 2; pass++) {
        await Promise.all(paths.map(p => fetch(`${baseURL}${p}`, { headers }).catch(() => {})));
    }
}

async function main() {
    const jobsJar = resolveJobsJar();
    log(`db target=${DB_TARGET}`);
    log(`image=${IMAGE}`);
    log(`jobs jar=${jobsJar}`);

    const network = await new Network().start();
    let db;
    let app;
    let exitCode = 1;

    try {
        log(`starting ${DB_TARGET} database…`);
        db = await new GenericContainer(target.db.image)
            .withNetwork(network)
            .withNetworkAliases(target.db.alias)
            .withEnvironment(target.db.env)
            .withWaitStrategy(Wait.forListeningPorts())
            .withStartupTimeout(180_000)
            .start();

        if (target.db.createDbCmd) {
            log("creating application database…");
            await ensureDatabase(db, target.db.createDbCmd, 180_000);
        }

        log("starting waltz app (liquibase + tomcat)…");
        app = await new GenericContainer(IMAGE)
            .withNetwork(network)
            .withNetworkAliases("waltz")
            .withEnvironment(target.appEnv)
            .withExposedPorts(APP_PORT)
            .withCopyFilesToContainer([{ source: jobsJar, target: "/loadall/uber-jobs.jar" }])
            .withCopyContentToContainer([
                { content: loadAllProps, target: "/loadall/waltz.properties" },
                { content: loadAllLogback, target: "/loadall/logback.xml" }
            ])
            .withWaitStrategy(Wait.forHttp("/", APP_PORT).forStatusCode(200))
            .withStartupTimeout(360_000)
            .start();

        if (process.env.E2E_SKIP_LOADALL === "1") {
            log("E2E_SKIP_LOADALL=1 — skipping sample-data generation");
        } else {
            log("seeding sample data via LoadAll (this can take a minute)…");
            const res = await app.exec([
                "java",
                "-Dlogback.configurationFile=/loadall/logback.xml",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.util=ALL-UNNAMED",
                "-cp", "/loadall:/loadall/uber-jobs.jar",
                "org.finos.waltz.jobs.generators.LoadAll"
            ]);
            if (res.exitCode !== 0) {
                console.error(res.output);
                throw new Error(`LoadAll failed with exit code ${res.exitCode}`);
            }
            log("restarting app so startup caches pick up the seeded data…");
            await app.restart();
        }

        const baseURL = `http://${app.getHost()}:${app.getMappedPort(APP_PORT)}`;
        await pollHttp(`${baseURL}/`, 120_000);
        log(`backend ready at ${baseURL}`);

        await warmUp(baseURL);

        const passthrough = process.argv.slice(2);
        log(`running: playwright test ${passthrough.join(" ")}`.trim());
        const run = spawnSync("npx", ["playwright", "test", ...passthrough], {
            cwd: e2eDir,
            stdio: "inherit",
            env: { ...process.env, WALTZ_BASE_URL: baseURL }
        });
        exitCode = run.status ?? 1;
    } finally {
        log("tearing down containers…");
        if (app) await app.stop().catch(() => {});
        if (db) await db.stop().catch(() => {});
        if (network) await network.stop().catch(() => {});
    }

    process.exit(exitCode);
}

main().catch(err => {
    console.error(err);
    process.exit(1);
});
