package com.nextlogin.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.nextlogin.core.BaseTest;
import com.nextlogin.core.TestUser;
import com.nextlogin.pages.AccountPage;
import com.nextlogin.pages.RegisterPage;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;

public class RegisterTest extends BaseTest {

  @Test(priority = 1, description = "Mismatched passwords are rejected by the server action")
  public void mismatchedPasswordsShowError() {
    RegisterPage page = new RegisterPage(driver).open();
    page.fill("Mismatch Case", "mismatch@example.com", "Passw0rd!23", "Passw0rd!99");
    page.submit();

    assertEquals(page.errorText(), "Passwords do not match");
  }

  @Test(priority = 2, description = "A password under 8 characters never leaves the browser")
  public void shortPasswordBlockedByConstraintValidation() {
    RegisterPage page = new RegisterPage(driver).open();
    page.fill("Short Case", "short@example.com", "abc123", "abc123");
    page.submit();

    // minLength keeps the form on /register with a native validation bubble
    assertEquals(page.currentPath(), "/register", "short password should not submit");
    assertNotEquals(
        page.passwordValidationMessage(), "", "browser should report a validation message");
  }

  @Test(
      priority = 3,
      groups = "register",
      description = "A fresh account is created and lands straight on the account page")
  public void registersFreshAccount() {
    RegisterPage page = new RegisterPage(driver).open();
    page.fill(TestUser.FULL_NAME, TestUser.EMAIL, TestUser.PASSWORD, TestUser.PASSWORD);
    page.submit();

    try {
      page.waitForPath("/");
    } catch (TimeoutException e) {
      // A bare "condition failed" tells you nothing; the page usually says exactly what went wrong.
      fail(
          "Sign-up did not reach the account page. Landed on "
              + page.currentPath()
              + (page.isPresent(RegisterPage.ALERT_TEXT)
                  ? " showing: " + page.errorText()
                  : " with no error alert")
              + ". If 'Confirm email' is on in Supabase, turn it off: the suite signs in"
              + " immediately after sign-up.");
    }

    AccountPage account = new AccountPage(driver);
    assertEquals(account.displayName(), TestUser.FULL_NAME);
    assertEquals(account.email(), TestUser.EMAIL);
    assertTrue(account.isPresent(AccountPage.LOGOUT_BUTTON), "log out button missing");

    TestUser.markRegistered();
  }
}
