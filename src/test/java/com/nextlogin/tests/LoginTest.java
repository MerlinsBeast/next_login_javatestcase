package com.nextlogin.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.nextlogin.core.BaseTest;
import com.nextlogin.core.TestUser;
import com.nextlogin.pages.AccountPage;
import com.nextlogin.pages.LoginPage;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

  private static final String SUPABASE_BAD_CREDENTIALS = "Invalid login credentials";

  @Test(
      dependsOnGroups = "register",
      description = "A wrong password is rejected and the alert is shown")
  public void wrongPasswordShowsError() {
    TestUser.requireRegistered();
    LoginPage page = new LoginPage(driver).open();
    page.fill(TestUser.EMAIL, "definitely-not-the-password");
    page.submit();

    // errorText() waits for the alert to appear; waitForPath("/login") would not, since the
    // browser is already on /login when the server action starts.
    assertEquals(page.errorText(), SUPABASE_BAD_CREDENTIALS);
  }

  @Test(description = "An email with no account is rejected the same way")
  public void unknownEmailShowsError() {
    LoginPage page = new LoginPage(driver).open();
    page.fill("nobody." + System.currentTimeMillis() + "@example.com", "Passw0rd!23");
    page.submit();

    assertEquals(page.errorText(), SUPABASE_BAD_CREDENTIALS);
  }

  @Test(
      dependsOnGroups = "register",
      description = "Valid credentials reach the account page with the right user details")
  public void validCredentialsShowAccountDetails() {
    AccountPage account = new LoginPage(driver).loginAs(TestUser.EMAIL, TestUser.PASSWORD);

    assertEquals(account.currentPath(), "/");
    assertEquals(account.displayName(), TestUser.FULL_NAME);
    assertEquals(account.email(), TestUser.EMAIL);
    assertEquals(account.detail("Sign-in method"), "email");
    assertFalse(account.detail("User ID").isBlank(), "user id row is empty");
    assertFalse(account.detail("Account created").equals("—"), "created date is empty");
    assertFalse(account.detail("Last sign-in").equals("—"), "last sign-in is empty");
  }

  @Test(dependsOnGroups = "register", description = "The account page is free of error alerts")
  public void accountPageHasNoAlert() {
    AccountPage account = new LoginPage(driver).loginAs(TestUser.EMAIL, TestUser.PASSWORD);
    assertFalse(account.isPresent(LoginPage.ALERT), "unexpected alert on the account page");
  }
}
