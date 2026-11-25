# JD QA Framework: Playwright (Java) + Cucumber BDD

A test automation framework covering:
- the **Swagger Petstore** REST API;
- the **OrangeHRM** web UI.

Both use one tool (Playwright for Java), one runner (Cucumber on JUnit 5) and one report.

**Stack:** Java 17 · Playwright for Java · Cucumber 7 (Gherkin BDD) · JUnit 5 Platform · Maven · AssertJ · Jackson

---

## 1. What is implemented

| # | Layer | Type | Scenario | Feature file |
|---|---|---|---|---|
| 1 | API | ✅ Positive | **Retrieve an existing pet**: create a pet, `GET /pet/{petId}`, expect 200, JSON, and the whole body equal to what was created | `features/api/get_pet_by_id.feature` |
| 2 | API | ❌ Negative | **Request a pet that does not exist**: expect 404 and `"Pet not found"` | `features/api/get_pet_by_id.feature` |
| 3 | UI | ✅ Positive | **Admin logs in and logs out**: Dashboard shown, user name in the top bar, logout returns to login | `features/ui/login.feature` |
| 4 | UI | ❌ Negative | **Login rejected for a wrong password**: "Invalid credentials" banner, user stays on the login page | `features/ui/login.feature` |

The scenarios are written in plain English (Gherkin), so business stakeholders, BAs and developers can read and review them without reading Java.

---

## 2. Setup and run

### Prerequisites
| Tool | Version | Check |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Internet | Maven Central, Playwright CDN, both demo sites | |

No environment variables or config files are needed: every setting has a working default.

### Run everything (one standard command)
```bash
mvn clean test
```
Playwright downloads its browsers automatically on the first run. To pre-install only Chromium:
```bash
mvn compile exec:java -Dexec.args="install chromium"
```

### Run a subset by tag
```bash
mvn test -Dcucumber.filter.tags="@api"        # API only (no browser, a few seconds)
mvn test -Dcucumber.filter.tags="@ui"         # UI only
mvn test -Dcucumber.filter.tags="@positive"   # happy paths
mvn test -Dcucumber.filter.tags="@negative"   # unhappy paths
mvn test -Dcucumber.filter.tags="@smoke"      # smoke pack
```

### Watch the browser
```bash
mvn test -Dcucumber.filter.tags="@ui" -Dheadless=false -DslowMo=500
```

### From IntelliJ
- **All scenarios:** open `src/test/java/com/qa/bdd/runner/RunCucumberTest.java` and click ▶ next to the class.
- **Tag filter or headed mode:** Edit Configurations → VM options, for example `-Dcucumber.filter.tags="@ui" -Dheadless=false`.
- **One feature or scenario:** with the *Cucumber for Java* plugin installed, click ▶ next to `Feature:` or `Scenario:` in a `.feature` file. Set Glue to `com.qa.bdd` if steps show as undefined.

### Configuration
Each value is resolved in this order: **system property → environment variable → default**.

| Property | Env var | Default |
|---|---|---|
| `petstore.baseUrl` | `PETSTORE_BASEURL` | `https://petstore.swagger.io/v2/` |
| `orangehrm.baseUrl` | `ORANGEHRM_BASEURL` | `https://opensource-demo.orangehrmlive.com` |
| `orangehrm.username` / `orangehrm.password` | `ORANGEHRM_USERNAME` / `ORANGEHRM_PASSWORD` | Public demo credentials (`Admin` / `admin123`) |
| `browser` | `BROWSER` | `chromium` (also `firefox`, `webkit`) |
| `headless` | `HEADLESS` | `true` |
| `slowMo` | `SLOWMO` | `0` ms |

In a real project the credentials would have no defaults; they would come from a CI secret store or vault.

### Reports
| Output | Location |
|---|---|
| Console | Every Gherkin step coloured pass/fail, plus each API request and response |
| **Cucumber HTML report** | `target/cucumber-reports/cucumber.html`: open it in a browser. Shows features, scenarios and steps, and attaches a screenshot (UI) or the last API response on failure |
| Cucumber JSON / JUnit XML | `target/cucumber-reports/cucumber.json`, `cucumber.xml`, for CI tools |
| Screenshot (failure only) | `target/screenshots/*.png` |
| Playwright trace (failure only) | `target/traces/*.zip`. Open with `mvn exec:java -Dexec.args="show-trace target/traces/<file>.zip"` or at https://trace.playwright.dev |

---

## 3. Project structure

```
src/main/java/com/qa/                    FRAMEWORK: reusable, no tests, no Cucumber dependency
├── config/Config.java                   property → env → default resolution
├── api/
│   ├── client/PetApiClient.java         Service Object for /pet (tests never build URLs/headers)
│   ├── model/                           Pet, Category, Tag, PetStatus, ApiError (Java records)
│   ├── builder/PetBuilder.java          Test Data Builder, unique ids per run
│   └── support/                         Json (Jackson), Retry (bounded polling)
├── ui/
│   ├── pages/                           Page Objects: BasePage, LoginPage, DashboardPage
│   ├── components/TopBar.java           Component Object shared by authenticated pages
│   ├── data/UserCredentials.java        test user (password masked in logs)
│   └── support/UrlPatterns.java         JS-safe URL regexes for Playwright assertions
└── reporting/ReportLog.java             step/request logging used by clients and pages

src/test/resources/features/             WHAT: business-readable scenarios (Gherkin)
├── api/get_pet_by_id.feature
└── ui/login.feature

src/test/java/com/qa/bdd/                GLUE: maps Gherkin onto the framework
├── runner/RunCucumberTest.java          JUnit 5 Suite → Cucumber engine + report plug-ins
├── hooks/Hooks.java                     per-scenario setup/teardown by tag (@api / @ui), failure evidence
├── context/                             ApiContext, UiContext (per-scenario state), PlaywrightManager
└── steps/                               PetSteps, LoginSteps: thin steps, assertions only
```

The three layers change for different reasons:
- **Features** change when business behaviour changes.
- **Page objects and clients** change when the application's UI or API changes.
- **Steps** stay small and rarely change.

---

## 4. Test Design Explanation

### 4.1 Design patterns and why

| Pattern | Where | Why |
|---|---|---|
| **BDD (Gherkin)** | `*.feature` | Shared understanding and living documentation. Tags drive which scenarios run in each pipeline stage. |
| **Service Object** | `PetApiClient` | Tests use domain operations (`createPet`, `getPetById`). It returns the raw response instead of throwing on 4xx, so negative tests can assert on the error. |
| **Page Object + Component Object** | `LoginPage`, `DashboardPage`, `TopBar` | Locators live in one place. Pages expose actions and locators but **no assertions**, so the same page serves positive and negative tests. Fluent returns document the journey: `loginAs()` returns `DashboardPage`, `attemptLogin()` stays on `LoginPage`. |
| **Test Data Builder** | `PetBuilder` | Valid data by default, overriding only what a scenario cares about. Unique ids keep tests isolated on a shared public sandbox. |
| **Scenario context + dependency injection** | `ApiContext`, `UiContext` (PicoContainer) | Steps share per-scenario state with no static fields: the pet created in a *Given* is the one fetched in the *When*. |
| **Tagged hooks** | `Hooks` | API scenarios never launch a browser. Every UI scenario gets a fresh browser context, so there is no shared session between tests. On failure a screenshot and trace are captured automatically. |
| **Externalised config** | `Config` | The same build runs locally, in CI or against another environment with no code change. |

### 4.2 Why these scenarios

**API: `GET /pet/{petId}`**
- **Positive:** the pet is created through the API first, so the test owns its data. The whole response is compared with what was created (status, content type and every field). This catches dropped or changed fields that spot-checks miss.
- **Negative:** "not found" is the most common error consumers handle, and they branch on both the status and the error body. The test asserts both: `404` and `"Pet not found"`.
- **Robustness:** the public Petstore is shared and slow to sync between its servers. The *Given* step briefly polls until the created pet is visible (no fixed sleeps), so the *When* is one clean call. Created pets are deleted in an `@After` hook.

**UI: OrangeHRM login**
- **Positive:** login is the gateway to every other journey, so it's the highest-value smoke check. The scenario covers the full session lifecycle (login, landing page, identity shown, logout), because logging in without being able to log out is itself a defect.
- **Negative:** a wrong password must show a generic error and keep the user on the login page. A generic message stops attackers learning which usernames exist.
- **Interactions:**
  - Locators prefer user-facing roles and names, then stable attributes, and use CSS classes only as a last resort.
  - All checks use Playwright's auto-waiting web-first assertions, so there is no `Thread.sleep`.
  - Every scenario gets a fresh browser context.
  - A trace is recorded for every scenario and kept only on failure.

### 4.3 API layer vs UI layer

Test each behaviour at the **lowest layer where its risk lives**. A UI test costs seconds, depends on rendering and timing, and fails with vague errors. An API test costs milliseconds and names the exact endpoint, payload and response.

| Behaviour | Best layer | Why |
|---|---|---|
| Login journey works end to end | **UI** (1 test) | The journey itself is what's being proved. |
| Every invalid-credential combination | **API** (auth endpoint) | The rule lives in the auth service. The UI only needs to prove the banner renders, once. |
| Field and password rules | **Unit/component**, enforced at **API** | Pure logic. The server must enforce it even if the UI is bypassed. |
| Role-based access (employee can't reach Admin) | **API first** (expect 403) + 1 UI check | Security must be server-side. The UI check only proves the menu is hidden. |
| "New employee appears in the list" | **API to set up, UI to verify** | Never build test data by clicking through forms you aren't testing. |
| Logging in before every UI test | **API / saved session** | Log in once and reuse the session; only the login test uses the login form. |

The result is faster feedback (the API suite can gate every pull request), fewer flaky tests, more precise failure messages, and tests that can be written before the UI exists.

---

## 5. Roadmap: P1 and P2 scenarios to add with more time

**Prioritisation:**
- Risk = business impact × likelihood of failure, adjusted for the cost to automate and the right test layer.
- **P1** = next sprint, gates every pull request (smoke + critical).
- **P2** = core regression, nightly.
- Every area covers both ✅ positive and ❌ negative paths.

### 5.1 API: Swagger Petstore

| Pri | Endpoint / area | ✅ Positive scenarios | ❌ Negative scenarios | # |
|---|---|---|---|---|
| **P1** | Response schema (all endpoints) | Every 2xx and 4xx body matches the `swagger.json` definitions | Missing or unexpected fields are flagged | 1 |
| **P1** | `POST /pet` (create) | Create with required fields only; create with every field | Missing `name`; missing `photoUrls`; malformed JSON; wrong `Content-Type` (415); wrong data type (`"id":"abc"`) | 7 |
| **P1** | `PUT /pet` (update) | Updated name/status is visible in a later `GET` | Updating a pet that doesn't exist should give 404 (the sandbox creates it instead, so raise a defect); invalid body | 3 |
| **P1** | `DELETE /pet/{petId}` | Delete an existing pet, then `GET` returns 404 | Delete twice (second gives 404); malformed id; `api_key` header handling | 4 |
| **P1** | `GET /pet/{petId}` (extend current) | n/a (covered) | Deleted pet returns 404; malformed ids (`abc`, `1.5`, int64 overflow) return a 4xx, never a 5xx | 2 |
| **P2** | `GET /pet/findByStatus` | Each status returns **only** pets with that status (Scenario Outline over 3 statuses); several statuses at once | Invalid status value; missing `status` parameter | 4 |
| **P2** | Store orders (`/store/order`, `/store/inventory`) | Place an order; get it back; delete it; inventory count changes | `orderId` 0 and 11 (just outside the documented 1–10 range); negative quantity; order for a pet that doesn't exist; invalid `shipDate` | 8 |
| **P2** | General HTTP behaviour | Correct headers on every response | `PATCH /pet` gives 405; XML `Accept` header; oversized payload; Unicode/emoji in names | 4 |
| | | | **API P1 + P2 total** | **33** |

### 5.2 UI: OrangeHRM

| Pri | Area | ✅ Positive scenarios | ❌ Negative scenarios | # |
|---|---|---|---|---|
| **P1** | Login (extend current) | n/a (covered) | Unknown username; case-sensitive password; empty username/password ("Required"); open a protected page while logged out (redirect to login) | 4 |
| **P1** | Role-based access | Admin sees the Admin menu | Employee (ESS) user can't see Admin/PIM; an ESS user who types the Admin URL is blocked; the ESS API call returns 403 | 4 |
| **P1** | PIM: Add Employee | Add employee, then find them in the Employee List | Missing first name; missing last name; duplicate Employee Id; wrong photo type; photo too large | 6 |
| **P1** | Admin: User Management | Add a system user and find them in search; the new user can log in | Password and confirmation differ; weak password; duplicate username; employee doesn't exist; a disabled user can't log in | 7 |
| **P2** | Session management | Logout ends the session | Browser Back after logout shows no data; session timeout; Forgot Password with an unknown username | 4 |
| **P2** | Leave: apply and approve | Employee applies, Admin approves, status updates in both views | Overlapping dates; not enough balance; end date before start date | 4 |
| **P2** | Admin: Job Titles | Add, edit and delete (with the confirm dialog) a job title | Duplicate title; cancel on the delete dialog keeps the record; maximum-length description | 6 |
| **P2** | Tables: search, sort, paginate | Filter narrows results; sort order is correct; page counts are correct | No matches gives "No Records Found"; special characters in search | 5 |
| | | | **UI P1 + P2 total** | **40** |

**In total, about 73 P1 + P2 scenarios** (38 P1 and 35 P2), roughly 2–3 weeks of automation effort. P3/P4 items come after, as capacity allows:
- user and upload endpoints;
- cross-browser runs;
- accessibility scans (axe-core);
- visual regression;
- security and performance testing with specialists.

---

## 6. How the roadmap would be automated

The framework is already layered for this: **new scenarios mostly mean a new feature file, a few thin steps, and one new client or page object.** No changes to the runner, hooks or config are needed.

### 6.1 API scenarios
1. **One client per resource**, following `PetApiClient`: `StoreApiClient` and `UserApiClient`. Each returns the raw `APIResponse`, so the same client serves positive and negative tests.
2. **Builders for valid data, with "break one rule" variants.** For example, `PetBuilder.aPet().without("name")`, or a raw-JSON overload for malformed bodies. Negative tests then change exactly one thing from a known-good request.
3. **Scenario Outlines for the negative matrix.** Each invalid input is a table row, not new code:
   ```gherkin
   @api @negative @P1
   Scenario Outline: Creating a pet is rejected when <field> is missing
     Given a valid pet without "<field>"
     When I create the pet
     Then the response status should be 400
     Examples:
       | field     |
       | name      |
       | photoUrls |
   ```
4. **Schema validation in one shared step:** `Then the response should match the "Pet" schema`. It uses a JSON-schema validator (e.g. `networknt/json-schema-validator`) with schemas generated from `swagger.json`, so contract drift on any endpoint is caught.
5. **Data isolation stays the same:** unique ids from the builders, cleanup in `@After`, and `Retry` polling for the shared sandbox's delays.
6. **Spec mismatches** (for example `PUT` creating a pet that doesn't exist) are logged as defects. The test asserts the agreed behaviour, not the bug.

### 6.2 UI scenarios
1. **Log in once, reuse the session.** Log in once in a `@BeforeAll` hook, save Playwright's `storageState`, and start each UI scenario already authenticated. Only `login.feature` uses the form. This saves several seconds per scenario and isolates login failures.
2. **Set up data through the API, verify in the UI.** An `OrangeHrmApiClient` (OrangeHRM exposes `/web/index.php/api/v2/...`) creates employees, users and leave entitlements before the scenario and deletes them after. The browser is used only for the behaviour under test.
3. **New page objects follow the existing pattern.** `EmployeeListPage`, `AddEmployeePage`, `UserManagementPage` and `JobTitlesPage` get actions and locators only. Shared widgets (data table, confirm dialog, toast message) become reusable components, like `TopBar`.
4. **Multi-user journeys** (leave apply and approve, role-based access) use **two browser contexts in one scenario**, Admin and Employee, each with its own saved session.
5. **The negative matrix** uses Scenario Outlines again, with each validation message as a table row.

### 6.3 Execution strategy
| Pipeline stage | Tag expression | Runs | Target time |
|---|---|---|---|
| Pull request gate | `@smoke or (@api and @P1)` | Smoke + all P1 API | < 3 min |
| Merge to main | `@P1` | All P1 (API + UI) | < 10 min |
| Nightly regression | `@P1 or @P2` | Everything | < 30 min |

- **Parallel runs:** enable Cucumber's parallel execution, with Playwright objects held per thread (Playwright for Java isn't thread-safe).
- **Reporting:** add Allure for history and trends in CI; keep the Cucumber HTML report for local runs.
- **Flaky tests:** retry once in CI, *reported* as flaky rather than hidden.

---

## 7. Challenges and trade-offs

| Challenge | How it was handled |
|---|---|
| **Shared public sandboxes.** Other users change data, and Petstore is slow to sync between servers. | Unique ids, self-cleaning scenarios, and bounded polling of the specific call (not whole-test retries, which would hide real defects). |
| **Playwright for Java evaluates URL regexes in the browser as JavaScript.** `Pattern.quote()` produces `\Q…\E`, which JavaScript doesn't understand, so a correct URL failed to match. | Added `UrlPatterns`, which escapes characters in a way both Java and JavaScript accept. The fix is in one place and no test needs to know about it. |
| **Cucumber expression syntax.** `(s)` in a step definition means "optional s", so it didn't match the literal text `message(s)`. | Escaped it as `\\(s)`. Lesson: treat step text as a pattern language, not plain text. |
| **BDD adds a layer of indirection.** | Kept steps thin; all logic lives in the framework layer, which has no Cucumber dependency. |
| **No `data-testid` attributes in OrangeHRM.** | Roles and `name` attributes where possible; fragile CSS classes kept inside page objects. In a real team I'd ask developers to add test ids. |
| **One-hour timebox.** | Prioritised a clean, extensible structure plus a well-designed positive and negative test per layer over breadth. The roadmap above shows what comes next. |
