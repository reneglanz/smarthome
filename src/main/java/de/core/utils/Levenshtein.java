package de.core.utils;

import java.util.Arrays;

public class Levenshtein {
  private static final Integer threshold = null;
  
  public static Integer compare(CharSequence left, CharSequence right) {
    if (threshold != null)
      return Integer.valueOf(limitedCompare(left, right, threshold.intValue())); 
    return Integer.valueOf(unlimitedCompare(left, right));
  }
  
  private static int limitedCompare(CharSequence left, CharSequence right, int threshold) {
    if (left == null || right == null)
      throw new IllegalArgumentException("CharSequences must not be null"); 
    if (threshold < 0)
      throw new IllegalArgumentException("Threshold must not be negative"); 
    int n = left.length();
    int m = right.length();
    if (n == 0)
      return (m <= threshold) ? m : -1; 
    if (m == 0)
      return (n <= threshold) ? n : -1; 
    if (n > m) {
      CharSequence tmp = left;
      left = right;
      right = tmp;
      n = m;
      m = right.length();
    } 
    if (m - n > threshold)
      return -1; 
    int[] p = new int[n + 1];
    int[] d = new int[n + 1];
    int boundary = Math.min(n, threshold) + 1;
    for (int i = 0; i < boundary; i++)
      p[i] = i; 
    Arrays.fill(p, boundary, p.length, 2147483647);
    Arrays.fill(d, 2147483647);
    for (int j = 1; j <= m; j++) {
      char rightJ = right.charAt(j - 1);
      d[0] = j;
      int min = Math.max(1, j - threshold);
      int max = (j > Integer.MAX_VALUE - threshold) ? n : Math.min(n, j + threshold);
      if (min > 1)
        d[min - 1] = Integer.MAX_VALUE; 
      for (int k = min; k <= max; k++) {
        if (left.charAt(k - 1) == rightJ) {
          d[k] = p[k - 1];
        } else {
          d[k] = 1 + Math.min(Math.min(d[k - 1], p[k]), p[k - 1]);
        } 
      } 
      int[] tempD = p;
      p = d;
      d = tempD;
    } 
    if (p[n] <= threshold)
      return p[n]; 
    return -1;
  }
  
  private static int unlimitedCompare(CharSequence left, CharSequence right) {
    if (left == null || right == null)
      throw new IllegalArgumentException("CharSequences must not be null"); 
    int n = left.length();
    int m = right.length();
    if (n == 0)
      return m; 
    if (m == 0)
      return n; 
    if (n > m) {
      CharSequence tmp = left;
      left = right;
      right = tmp;
      n = m;
      m = right.length();
    } 
    int[] p = new int[n + 1];
    int i;
    for (i = 0; i <= n; i++)
      p[i] = i; 
    for (int j = 1; j <= m; j++) {
      int upperLeft = p[0];
      char rightJ = right.charAt(j - 1);
      p[0] = j;
      for (i = 1; i <= n; i++) {
        int upper = p[i];
        int cost = (left.charAt(i - 1) == rightJ) ? 0 : 1;
        p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperLeft + cost);
        upperLeft = upper;
      } 
    } 
    return p[n];
  }
  
  public static void main(String[] args) {
    System.out.println(compare("tisch lampe", "stehlampe"));
    System.out.println(compare("tisch lampe", "tischlampe"));
  }
}
