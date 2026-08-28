package com.nextlogin.core;

/** One throwaway Supabase account, created by RegisterTest and reused by the rest of the suite. */
public final class TestUser {

  public static final String PASSWORD = "Passw0rd!23";
  public static final String FULL_NAME = "Ada Lovelace";

  /** Unique per JVM run so a suite can be re-run without colliding with an existing account. */
  public static final String EMAIL = "qa.next.login+" + System.currentTimeMillis() + "@gmail.com";

  private static boolean registered = false;

  public static void markRegistered() {
    registered = true;
  }

  public static void requireRegistered() {
    if (!registered) {
      throw new IllegalStateException(
          "No account was registered. Run the full suite via testng.xml so RegisterTest goes first.");
    }
  }

  private TestUser() {}
}
