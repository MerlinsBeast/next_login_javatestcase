package com.nextlogin.tests;

import static org.testng.Assert.assertEquals;

import com.nextlogin.core.BaseTest;
import com.nextlogin.pages.LoginPage;
import org.testng.annotations.Test;

/**
 * Deliberately red. It exists only to prove the Jenkins job reports failures rather than
 * silently going green, and to give the JUnit test report a failure to render.
 *
 * <p>DELETE THIS CLASS, and its entry in testng.xml, once you have seen a red build.
 */
public class DeliberateFailureTest extends BaseTest {

  @Test(description = "Always fails on purpose: the real heading is 'Welcome back'")
  public void headingTextIsWrongOnPurpose() {
    LoginPage page = new LoginPage(driver).open();

    assertEquals(
        page.textOf(LoginPage.HEADING),
        "Welcome back, friend",
        "This assertion is wrong on purpose. See the class comment.");
  }
}
