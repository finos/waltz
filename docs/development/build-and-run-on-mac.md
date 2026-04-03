# Setting up Waltz development environment on MacBook with PostgreSQL
Welcome! This guide is to walk you through setting up your development environment on MacOS.

This document describes how to:
1. install Homebrew package manager
1. install git
1. install Java Development Kit 17 (OpenJDK)
1. install maven
1. install Node.js
1. install PostgreSQL and create the Waltz database
1. configure maven profile for PostgreSQL database connection
1. build Waltz
1. run Waltz

This guide is made on 3rd February, 2023 with all versions of the components being latest at this point in time. As you may use this guide later in time, the versions can be different, but the following order and logic will be still applicable.

# Step 1. Install Homebrew

Waltz uses [Homebrew](https://brew.sh/) to install dependencies on macOS.

**Check if Homebrew is installed:**
```bash
brew --version
```

**Install Homebrew** (if not already installed):

Visit https://brew.sh/ and follow the installation instructions.

# Step 2. Install git

macOS typically comes with git pre-installed.

**Check if git is installed:**
```bash
git --version
```

**Install git** (if not already installed):
```bash
brew install git
```

# Step 3. Install JDK 17

Waltz requires Java Development Kit 17 (OpenJDK). **Java 17 is required** - newer versions (18+) may cause annotation processing failures during compilation.

**Check if JDK 17 is installed:**
```bash
java -version
```

**Install JDK 17** (if not already installed):
```bash
brew install openjdk@17
```

**Set JAVA_HOME** (add to your `~/.zshrc` or `~/.bashrc`):
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
```

> **Important:** If you have multiple Java versions installed, Maven will use whatever version is set in `JAVA_HOME`. If you see compilation errors about missing `Immutable*` classes (e.g., `ImmutableEntityReference`, `TransportKindValue`), it's likely because you're using a Java version newer than 17. Verify with `mvn --version` that Java 17 is being used.

# Step 4. Install Maven

Maven is used to build Waltz. It must be configured to use JDK 17.

**Check if Maven is installed:**
```bash
mvn --version
```

Verify the output shows `Java version: 17.x.x`. If it shows a different Java version, ensure `JAVA_HOME` is set correctly.

**Install Maven** (if not already installed):
```bash
brew install maven
```


# Step 5. Install Node.js

Node.js is required for building the Waltz front-end.

**Check if Node.js is installed:**
```bash
node --version
```

**Install Node.js** (if not already installed):
```bash
brew install node
```

# Step 6. Install PostgreSQL

**Check if PostgreSQL is installed:**
```bash
psql -V
```

**Install PostgreSQL** (if not already installed):
```bash
brew install postgresql
```

## Initialize and Start the Database

**Create a folder for database files and initialize:**
```bash
mkdir waltz-db
initdb -D ./waltz-db
```

**Start the PostgreSQL server:**
```bash
pg_ctl -D ./waltz-db -l waltz-db.log start
```

**Create the Waltz database:**
```bash
psql postgres -c "create database waltz"
```

# Step 7. Configure Maven Profile

Waltz uses Maven profiles to provide database connection details for jOOQ code generation and Liquibase schema management. You need to configure **two profiles**:

1. **Database vendor profile** (defined in the project's `pom.xml`): `waltz-postgres`, `waltz-h2`, `waltz-mariadb`, or `waltz-mssql`
2. **Database connection profile** (defined in your local `~/.m2/settings.xml`): Contains your specific connection details

**Create or edit `~/.m2/settings.xml`:**

```xml
<settings>
    <profiles>
        <profile>
            <id>dev-postgres</id>
            <properties>
                <database.url>jdbc:postgresql://localhost:5432/waltz</database.url>
                <database.user>your_username</database.user>
                <database.password></database.password>
                <database.schema>public</database.schema>
            </properties>
        </profile>
    </profiles>
</settings>
```

Replace `your_username` with your macOS username (or the PostgreSQL user you created).

> **Important:** Without this configuration, the Maven build will fail with errors like:
> - `'dependencies.dependency.groupId' for ${jooq.group}:jooq:jar with value '${jooq.group}' does not match a valid id pattern`
> - `Property 'database.url' must be specified`
>
> These errors indicate that the required Maven profiles are not activated or configured.

# Step 8. Build Waltz

**Clone the Waltz repository:**
```bash
git clone git@github.com:finos/waltz.git
cd waltz
```

**Build Waltz:**
```bash
mvn clean compile -P waltz-postgres,dev-postgres
```

> **Note:** You must specify both profiles:
> - `waltz-postgres` - the database vendor profile (from `pom.xml`)
> - `dev-postgres` - your connection profile (from `~/.m2/settings.xml`)

The build may take a few minutes. A successful build will end with `BUILD SUCCESS`.

**Alternative: Quick start with H2 in-memory database**

If you don't want to set up PostgreSQL, you can use the H2 in-memory database profile for a quick build:
```bash
mvn clean compile -P waltz-h2,dev-h2-inmem
```

> **Note:** The H2 profile is useful for development and testing, but data will not persist between runs.
# Step 9. Run Waltz

**Package Waltz:**
```bash
mvn clean package -P waltz-postgres,dev-postgres
```

## Configure Waltz Properties

**Create `~/.waltz/waltz.properties`:**
```properties
database.url=jdbc:postgresql://localhost:5432/waltz
database.user=your_username
database.password=
database.schema=waltz
database.driver=org.postgresql.Driver
jooq.dialect=POSTGRES

database.pool.max=16

waltz.from.email=your_email@example.com
waltz.base.url=http://localhost:8443/
```

Replace `your_username` and `your_email@example.com` with your details.

**Start Waltz:**
```bash
$JAVA_HOME/bin/java -cp ./waltz-web/target/waltz-web-jar-with-dependencies.jar org.finos.waltz.web.Main
```

> **Note:** If you get a "Java not found" error, ensure `JAVA_HOME` is set correctly (see Step 3).

# Congratulations!

Waltz is now running! Open [http://localhost:8443/](http://localhost:8443/) in your browser.