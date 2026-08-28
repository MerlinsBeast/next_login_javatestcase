# next_login XPath UI tests

Selenium 4 + TestNG + Maven. Page Object Model. Every locator is an XPath.

Tests the app at **https://github.com/yajay0411/next_login**. Clone it as a sibling folder,
or point the suite anywhere with `-DbaseUrl=`.

## Prerequisites

- JDK 21 and Maven on the PATH
- Chrome installed (Selenium Manager fetches the matching chromedriver automatically)
- A Supabase backend with **email confirmation OFF**. The suite registers a throwaway account
  each run and signs in with it immediately, which an emailed confirmation link would block.
- The app running against that backend

The local stack is the easy path, and it defaults to confirmation off:

```bash
git clone https://github.com/yajay0411/next_login.git ../next_login
cd ../next_login
npm install
supabase start          # ports remapped to 563xx so it will not collide with other stacks
npm run dev
```

Copy `next_login/.env.example` to `.env.local` and fill in the API URL and publishable key
that `supabase start` prints. For a cloud project instead, use the values from Project
Settings → API Keys, and turn off Confirm email under Authentication → Sign In / Providers.

## Run

```bash
mvn test                                  # headless against http://localhost:3000
mvn test -Dheadless=false                 # watch it drive the browser
mvn test -DbaseUrl=https://staging.example.com
mvn test -Dtest=LoginTest                 # one class
```

## Layout

```
core/BaseTest.java        fresh Chrome per test method, baseUrl and headless flags
core/TestUser.java        the unique throwaway account for this run
pages/BasePage.java       XPath helpers: visible, type, click, waitForPath
pages/LoginPage.java      XPaths + actions for /login
pages/RegisterPage.java   XPaths + actions for /register
pages/AccountPage.java    XPaths + actions for /
tests/ElementPresenceTest every field, label, button and link on both auth pages
tests/RegisterTest        mismatch, short password, successful sign-up
tests/LoginTest           wrong password, unknown email, successful sign-in details
tests/GuardTest           both redirect directions, plus logout clearing the session
```

## Ordering

`RegisterTest.registersFreshAccount` belongs to the TestNG group `register`. Every test that
needs a real session declares `dependsOnGroups = "register"`, so run the whole suite through
`testng.xml` rather than cherry-picking a single login test.

## Reporting failures to xpath_healer

`ci/notify_xpath_healer.py` posts the build result to the xpath_healer service, which records
every delivery and decides whether a failure looks like XPath drift rather than a real bug.

It reads Jenkins' own environment plus three variables:

```
XPATH_HEALER_URL     e.g. http://localhost:3002/api/v1/webhooks/jenkins
XPATH_HEALER_SECRET  sent as the X-Webhook-Secret header
BUILD_RESULT         SUCCESS | FAILURE | UNSTABLE
```

Failures come from `target/surefire-reports/TEST-*.xml`, so run it after Maven, not instead of
it. The delivery id is `JOB_NAME#BUILD_NUMBER`, which makes a replayed delivery a no-op rather
than a duplicate row.

The notifier never fails the build. If the service is down or the secret is wrong it prints the
problem and exits zero, because a broken notifier must not turn a green build red. With
`XPATH_HEALER_URL` unset it skips entirely, so the suite still runs anywhere.

## Waiting

`BasePage.waitForPath` compares the **parsed path exactly**. It deliberately does not use
`urlContains`, because `urlContains("/")` is true of every URL and waits for nothing, and
`urlContains("/login")` is already true before a form on `/login` has submitted. Where an
assertion depends on a server action finishing, wait on the thing that appears, such as
`errorText()`, not on a path you are already standing on.

The default wait is 30 seconds, which absorbs the Next dev server compiling a route on
first hit.

## Notes on two locators

The password field carries `minLength=8`, so a short password is stopped by the browser and
never reaches the server. That test asserts the native `validationMessage` instead of an alert.

Error text comes from Supabase, so `LoginTest` pins the literal string `Invalid login
credentials`. If Supabase reworks its messages, that constant is the one place to update.
