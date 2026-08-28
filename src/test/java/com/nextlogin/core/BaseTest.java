package com.nextlogin.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * One fresh Chrome per test method, so no test inherits another test's session cookie.
 *
 * <p>Override defaults with -DbaseUrl=... and -Dheadless=false.
 */
public abstract class BaseTest {

  /**
   * Strong references are required. Logger.getLogger hands back a weakly-referenced logger, so
   * a level set on a logger nobody holds is discarded at the next GC and the warnings return.
   */
  private static final List<Logger> SILENCED =
      Stream.of(
              "org.openqa.selenium.devtools.CdpVersionFinder",
              "org.openqa.selenium.chromium.ChromiumDriver")
          .map(Logger::getLogger)
          .peek(logger -> logger.setLevel(Level.SEVERE))
          .toList();

  protected WebDriver driver;

  public static String baseUrl() {
    return System.getProperty("baseUrl", "http://localhost:3000");
  }

  /** One clear message beats sixteen identical connection-refused stack traces. */
  @BeforeSuite(alwaysRun = true)
  public void appMustBeReachable() {
    HttpRequest probe =
        HttpRequest.newBuilder(URI.create(baseUrl() + "/login"))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

    // The Next dev server closes the connection on Java's default HTTP/2 upgrade attempt.
    try (HttpClient http =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build()) {
      HttpResponse<String> response = http.send(probe, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 500) {
        throw new IllegalStateException(
            baseUrl()
                + "/login returned "
                + response.statusCode()
                + ". The app is up but erroring, most likely because SUPABASE_URL and "
                + "SUPABASE_PUBLISHABLE_KEY are missing from next_login/.env.local.");
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not reach " + baseUrl() + " (" + e.getMessage() + "). "
              + "Start the app with: cd ../next_login && npm run dev",
          e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while probing " + baseUrl(), e);
    }
  }

  @BeforeMethod
  public void startBrowser() {
    ChromeOptions options = new ChromeOptions();
    if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
      options.addArguments("--headless=new");
    }
    options.addArguments("--window-size=1280,900", "--disable-gpu", "--no-sandbox");

    // Selenium Manager downloads the matching chromedriver; no WebDriverManager needed.
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ZERO);
  }

  @AfterMethod(alwaysRun = true)
  public void stopBrowser() {
    if (driver != null) {
      driver.quit();
    }
  }
}
