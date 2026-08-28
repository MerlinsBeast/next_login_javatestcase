package com.nextlogin.pages;

import com.nextlogin.core.BaseTest;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {

  public static final String PATH = "/register";

  // ---- XPath locators -------------------------------------------------
  public static final String HEADING =
      "//div[@data-slot='card-title' and normalize-space()='Create your account']";
  public static final String SUBHEADING =
      "//div[@data-slot='card-description'"
          + " and normalize-space()='It takes less than a minute.']";
  public static final String NAME_LABEL = "//label[@for='name']";
  public static final String NAME_INPUT = "//input[@id='name']";
  public static final String EMAIL_LABEL = "//label[@for='email']";
  public static final String EMAIL_INPUT = "//input[@id='email' and @type='email']";
  public static final String PASSWORD_LABEL = "//label[@for='password']";
  public static final String PASSWORD_INPUT = "//input[@id='password' and @type='password']";
  public static final String PASSWORD_HINT =
      "//p[@data-slot='field-description' and normalize-space()='At least 8 characters.']";
  public static final String CONFIRM_LABEL = "//label[@for='confirm']";
  public static final String CONFIRM_INPUT = "//input[@id='confirm' and @type='password']";
  public static final String SUBMIT =
      "//button[@type='submit' and normalize-space()='Create account']";
  public static final String LOGIN_LINK =
      "//a[@href='/login' and normalize-space()='Sign in']";
  public static final String ALERT_TEXT =
      "//div[@role='alert']//div[@data-slot='alert-description']";

  public RegisterPage(WebDriver driver) {
    super(driver);
  }

  public RegisterPage open() {
    driver.get(BaseTest.baseUrl() + PATH);
    visible(SUBMIT);
    return this;
  }

  public RegisterPage fill(String name, String email, String password, String confirm) {
    type(NAME_INPUT, name);
    type(EMAIL_INPUT, email);
    type(PASSWORD_INPUT, password);
    type(CONFIRM_INPUT, confirm);
    return this;
  }

  public void submit() {
    click(SUBMIT);
  }

  public String errorText() {
    return textOf(ALERT_TEXT);
  }

  /** The browser blocks submission on minLength before the server ever sees it. */
  public String passwordValidationMessage() {
    return propertyOf(PASSWORD_INPUT, "validationMessage");
  }
}
