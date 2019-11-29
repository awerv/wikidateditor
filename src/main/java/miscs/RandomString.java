package main.java.miscs;

import java.security.SecureRandom;

public final class RandomString
{

  /* Assign a string that contains the set of characters you allow. */
  private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"; 

  private final SecureRandom random = new SecureRandom();

  private final char[] buf;

  public RandomString(int length)
  {
    if (length < 1)
      throw new IllegalArgumentException("length < 1: " + length);
    buf = new char[length];
  }

  public String nextString()
  {
    for (int idx = 0; idx < buf.length; ++idx) 
      buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
    return new String(buf);
  }

}