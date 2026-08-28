package com.nextlogin.pages;

import com.nextlogin.core.BaseTest;
import org.openqa.selenium.WebDriver;

public class AccountPage extends BasePage {

  public static final String PATH = "/";

  // ---- XPath locators -------------------------------------------------
  public static final String NAME = "//div[@data-slot='card-title']";
  public static final String EMAIL = "//div[@data-slot='card-description']";
  public static final String LOGOUT_BUTTON =
      "//button[@type='submit' and normalize-space()='Log out']";
  public static final String DETAIL_LIST = "//dl";

  /** Value cell sitting next to a detail label, e.g. row("User ID"). */
  public static String row(String label) {
    return "//dt[normalize-space()='" + label + "']/following-sibling::dd[1]";
  }

  public AccountPage(WebDriver driver) {
    super(driver);
  }

  public AccountPage open() {
    driver.get(BaseTest.baseUrl() + PATH);
    return this;
  }

  public String displayName() {
    return textOf(NAME);
  }

  public String email() {
    return textOf(EMAIL);
  }

  public String detail(String label) {
    return textOf(row(label));
  }

  public LoginPage logout() {
    click(LOGOUT_BUTTON);
    waitForPath("/login");
    return new LoginPage(driver);
  }
}
