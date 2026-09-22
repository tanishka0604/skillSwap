# SkillSwap

Trade what you know for what you want to learn. Members list skills they can **teach** and skills they want to **learn**. SkillSwap finds two-way matches, lets them send swap requests, and opens a chat once a request is accepted.

**Stack:** Java 17 · Servlets (javax, Tomcat 9) · MySQL 8 · plain HTML/CSS/JS.

You can run it in either of two ways. Both work the same on **Windows** and **macOS**:

| | Option A: Docker (easiest) | Option B: Without Docker |
|---|---|---|
| You install | Docker Desktop | Java 17 + MySQL 8 |
| Database setup | Automatic | Run 3 SQL files once |
| Start command | `docker compose up --build` | `mvnw package cargo:run` |

When it's running, open **http://localhost:8080/SkillSwap/**

---

## Option A: Run with Docker (recommended)

Docker runs MySQL and the app in containers, so there's nothing else to install or configure.

### 1. Install Docker Desktop
- **Windows:** https://docs.docker.com/desktop/setup/install/windows-install/. Keep the default **WSL 2** option, and restart when asked.
- **macOS:** https://docs.docker.com/desktop/setup/install/mac-install/. Pick Apple Silicon or Intel to match your Mac.

Open Docker Desktop and wait until it says **Engine running**.

### 2. Start SkillSwap
Open a terminal (**PowerShell** on Windows, **Terminal** on macOS) in the project folder and run:

```bash
docker compose up --build
```

The first run takes a few minutes because it downloads MySQL, Maven and Tomcat. It's ready when the log shows `Server startup in [...] milliseconds`. Then open **http://localhost:8080/SkillSwap/**.

The database tables are created automatically from the `sql/` folder the first time.

### Everyday commands
| What | Command |
|---|---|
| Start (in the background) | `docker compose up --build -d` |
| See logs | `docker compose logs -f app` |
| Stop | `docker compose down` |
| Stop **and wipe all data** (fresh database) | `docker compose down -v` |

Your data is kept between restarts. Only `down -v` deletes it.

---

## Option B: Run without Docker

You need **Java 17** and **MySQL 8**. You do **not** need to install Maven or Tomcat: the included Maven Wrapper (`mvnw`) and the Cargo plugin download them automatically.

### 1. Install Java 17 (JDK)

**Windows** (PowerShell):
```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```
Or download the `.msi` from https://adoptium.net/temurin/releases/?version=17. In the installer, turn on **"Set JAVA_HOME variable"**.

**macOS:**
```bash
brew install --cask temurin@17
```

Check it in a **new** terminal: `java -version` should print `17`.

### 2. Install MySQL 8

**Windows:** download **MySQL Installer** from https://dev.mysql.com/downloads/installer/ and choose **Server only** (or "Full" if you also want MySQL Workbench). Set a root password during setup and remember it.

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation   # set a root password
```

### 3. Create the database (one time only)

From the project folder, run the three scripts in order. You'll be asked for your MySQL root password once.

**Windows (PowerShell):**
```powershell
Get-Content sql\01_create_users_table.sql, sql\02_create_skill_tables.sql, sql\03_add_profile_fields.sql | mysql -u root -p
```
> If `mysql` is "not recognized", replace `mysql` with its full path:
> `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"`
>
> Or skip the command line: open each file in **MySQL Workbench** and run it (⚡), in order 01 → 02 → 03.

**macOS:**
```bash
cat sql/01_create_users_table.sql sql/02_create_skill_tables.sql sql/03_add_profile_fields.sql | mysql -u root -p
```

### 4. Tell the app your MySQL password

The app reads its database settings from `src/main/webapp/WEB-INF/classes/db.properties`. Instead of editing that file, set **environment variables** in the terminal you start the app from. They take priority over the file:

**Windows (PowerShell):**
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "your-mysql-password"
```

**Windows (Command Prompt):**
```bat
set DB_USER=root
set DB_PASSWORD=your-mysql-password
```

**macOS:**
```bash
export DB_USER=root
export DB_PASSWORD='your-mysql-password'
```

(`DB_URL` can be overridden the same way if your MySQL isn't on `localhost:3306`.)

### 5. Build and run

**Windows:**
```powershell
.\mvnw.cmd package cargo:run
```

**macOS:**
```bash
./mvnw package cargo:run
```

The first run downloads Maven and Tomcat 9, so it takes a minute or two. It's ready when you see `Tomcat 9.0.x started on port [8080]`. Open **http://localhost:8080/SkillSwap/**. Press **Ctrl + C** to stop.

After changing code, stop with Ctrl + C and run the same command again.

<details>
<summary>Already have your own Tomcat 9?</summary>

Run `mvnw package` (`.\mvnw.cmd package` on Windows), copy `target/SkillSwap.war` into Tomcat's `webapps/` folder, and start Tomcat. Set `DB_USER` / `DB_PASSWORD` as environment variables for Tomcat, or edit `db.properties`. Use **Tomcat 9**: Tomcat 10+ uses `jakarta.*` and will not run this app.
</details>

---

## Try it out

1. **Create two accounts**, for example one in a normal window and one in a private/incognito window.
2. On **Manage skills**, make them complement each other. For example, user 1 teaches *JavaScript* and wants *Photoshop*; user 2 teaches *Photoshop* and wants *JavaScript*.
3. User 1 opens **Matches**, sees user 2, and clicks **Send swap request**.
4. User 2 opens **Requests** and clicks **Accept**.
5. Both can now open **Chat** from Requests → *Accepted swaps*.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| "Something went wrong while creating your account" | The app can't reach MySQL. Check that MySQL is running, the 3 SQL files were run, and `DB_PASSWORD` is set in the **same** terminal you started the app from. The exact error is printed in that terminal / `docker compose logs app`. |
| `Port 8080 is already in use` / `address already in use` | Another program (often another Tomcat) is using 8080. Stop it, or in Docker mode change `"8080:8080"` to `"8081:8080"` in `docker-compose.yml` and open http://localhost:8081/SkillSwap/. |
| `Duplicate column name 'bio'` when running the SQL | You already ran `03_add_profile_fields.sql` before. That's fine, the database is already set up. |
| `'mvnw' is not recognized` on Windows | Use `.\mvnw.cmd`, run from the project folder. |
| `JAVA_HOME is not set` / wrong Java version | Reinstall Temurin 17 with "Set JAVA_HOME" on, then open a **new** terminal. |
| Pages look outdated after an update | Hard-refresh the browser: **Ctrl + Shift + R** (Windows) / **Cmd + Shift + R** (macOS). |
| Docker: "Cannot connect to the Docker daemon" | Start Docker Desktop and wait for *Engine running*. |

---

## Project layout

```
sql/                     Database scripts, run in order 01 → 02 → 03
src/main/java/com/skillswap/
  servlet/               HTTP endpoints (pages + JSON APIs)
  service/               Business rules (validation, matching, request state machine)
  dao/                   All SQL lives here (JDBC + PreparedStatement)
  model/                 Plain data classes and enums
  util/                  DB connection, password hashing, JSON, session helpers
src/main/webapp/
  *.html                 Public pages (landing, login, register)
  WEB-INF/pages/*.html   Logged-in pages (served only through their servlets)
  css/, js/              Styles and page scripts
Dockerfile, docker-compose.yml   Option A
mvnw, mvnw.cmd, .mvn/            Maven Wrapper for Option B
```
