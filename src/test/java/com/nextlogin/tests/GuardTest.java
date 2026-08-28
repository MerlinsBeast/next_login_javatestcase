package com.nextlogin.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.nextlogin.core.BaseTest;
import com.nextlogin.core.TestUser;
import com.nextlogin.pages.AccountPage;
import com.nextlogin.pages.LoginPage;
import com.nextlogin.pages.RegisterPage;
import org.testng.annotations.Test;

/** Route protection, both directions, plus logout. */
public class GuardTest extends BaseTest {

  @Test(description = "An anonymous visitor to the account page is sent to login")
  public void anonymousVisitorIsRedirectedToLogin() {
    AccountPage account = new AccountPage(driver).open();
    account.waitForPath("/login");

    assertEquals(account.currentPath(), "/login");
    assertTrue(
        new LoginPage(driver).isPresent(LoginPage.SUBMIT), "login form did not render");
  }

  @Test(description = "Register stays reachable while signed out")
  public void anonymousVisitorCanReachRegister() {
    RegisterPage page = new RegisterPage(driver).open();
    assertEquals(page.currentPath(), "/register");
  }

  @Test(
      dependsOnGroups = "register",
      description = "A signed-in user is bounced off the login page")
  public void signedInUserIsBouncedOffLogin() {
    new LoginPage(driver).loginAs(TestUser.EMAIL, TestUser.PASSWORD);

    driver.get(baseUrl() + LoginPage.PATH);
    AccountPage account = new AccountPage(driver);
    account.waitForPath("/");
    assertEquals(account.currentPath(), "/");
  }

  @Test(
      dependsOnGroups = "register",
      description = "A signed-in user is bounced off the register page")
  public void signedInUserIsBouncedOffRegister() {
    new LoginPage(driver).loginAs(TestUser.EMAIL, TestUser.PASSWORD);

    driver.get(baseUrl() + RegisterPage.PATH);
    AccountPage account = new AccountPage(driver);
    account.waitForPath("/");
    assertEquals(account.currentPath(), "/");
  }

  @Test(
      dependsOnGroups = "register",
      description = "Log out returns to login and the session is really gone")
  public void logoutEndsTheSession() {
    AccountPage account = new LoginPage(driver).loginAs(TestUser.EMAIL, TestUser.PASSWORD);
    LoginPage login = account.logout();

    assertEquals(login.currentPath(), "/login");

    // going back to the account page must bounce again, proving the cookie was cleared
    new AccountPage(driver).open().waitForPath("/login");
    assertEquals(login.currentPath(), "/login");
  }
}
