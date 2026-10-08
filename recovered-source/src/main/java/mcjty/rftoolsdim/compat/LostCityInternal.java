package mcjty.rftoolsdim.compat;

import java.util.function.Function;
import mcjty.lostcities.api.ILostCities;

public class LostCityInternal {
   static ILostCities lostCities = null;

   public static class GetLostCity implements Function<ILostCities, Void> {
      public Void apply(ILostCities tm) {
         LostCityInternal.lostCities = tm;
         return null;
      }
   }
}
