package com.nextlogin.core;

import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Dumps the live DOM next to the surefire report when a test fails, so CI can ship it to
 * xpath_healer. Without this the healer sees only an exception string, which is not enough to
 * propose a replacement locator.
 *
 * <p>Registered in testng.xml. Every failure here is swallowed: a capture problem must never
 * change a test's own verdict.
 */
public class FailureCaptureListener implements ITestListener {

  static final Path DIR = Path.of("target", "failure-dom");

  @Override
  public void onTestFailure(ITestResult result) {
    // `driver` is protected, and this listener shares BaseTest's package.
    if (!(result.getInstance() instanceof BaseTest test)) return;
    WebDriver driver = test.driver;
    if (driver == null) return;

    // Matches the notifier's key: surefire's classname + name for this same testcase.
    String key = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    try {
      Files.createDirectories(DIR);
      Files.writeString(DIR.resolve(key + ".html"), driver.getPageSource());
      Files.writeString(DIR.resolve(key + ".url"), driver.getCurrentUrl());
    } catch (Exception e) {
      System.out.println("[capture] " + key + ": " + e);
    }
  }
}
