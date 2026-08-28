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
