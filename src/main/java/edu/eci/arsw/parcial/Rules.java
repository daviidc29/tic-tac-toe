package edu.eci.arsw.parcial;
public class Rules {
  private static final int[][] L = {
    {0,1,2},{3,4,5},{6,7,8},
    {0,3,6},{1,4,7},{2,5,8},
    {0,4,8},{2,4,6}
  };
  public static Character winner(Character[] b) {
    for (int[] r: L) {
      Character a=b[r[0]], c=b[r[1]], d=b[r[2]];
      if (a!=null && a==c && a==d) return a;
    }
    return null;
  }
}
