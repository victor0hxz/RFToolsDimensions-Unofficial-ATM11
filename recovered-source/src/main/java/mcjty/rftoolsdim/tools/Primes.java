package mcjty.rftoolsdim.tools;

import java.util.Random;

public class Primes {
   public static final long[] PRIMES = new long[]{
      900157L,
      981961L,
      50001527L,
      32667413L,
      1111114993L,
      65548559L,
      320741L,
      100002509L,
      35567897L,
      218021L,
      2900001163L,
      3399018867L,
      546151L,
      9890381L,
      666561271L,
      1666560437L,
      2556149L,
      64547713L,
      446455001329L,
      246454942523L
   };
   private static final Random random = new Random();
   private int idx = random.nextInt(PRIMES.length);

   public int nextInt() {
      int rc = (int)PRIMES[this.idx++];
      if (this.idx >= PRIMES.length) {
         this.idx = 0;
      }

      return rc;
   }

   public int nextIntUnsigned() {
      int rc = (int)PRIMES[this.idx++];
      if (this.idx >= PRIMES.length) {
         this.idx = 0;
      }

      if (rc < 0) {
         rc = -rc;
      }

      return rc;
   }

   public long nextLong() {
      long rc = PRIMES[this.idx++];
      if (this.idx >= PRIMES.length) {
         this.idx = 0;
      }

      return rc;
   }
}
