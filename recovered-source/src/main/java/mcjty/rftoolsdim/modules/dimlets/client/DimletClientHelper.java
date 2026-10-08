package mcjty.rftoolsdim.modules.dimlets.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import mcjty.lib.blockcommands.ISerializer;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class DimletClientHelper {
   public static long dimletListAge = 0L;
   public static List<DimletClientHelper.DimletWithInfo> dimlets = new ArrayList<>();

   public static void setDimletsOnGui(List<DimletClientHelper.DimletWithInfo> dimlets) {
      DimletClientHelper.dimlets = dimlets;
      dimletListAge++;
   }

   public record DimletWithInfo(DimletKey dimlet, boolean craftable) implements Comparable<DimletClientHelper.DimletWithInfo> {
      public int compareTo(DimletClientHelper.DimletWithInfo o) {
         return this.dimlet().compareTo(o.dimlet());
      }

      public static class Serializer implements ISerializer<DimletClientHelper.DimletWithInfo> {
         public Function<RegistryFriendlyByteBuf, DimletClientHelper.DimletWithInfo> getDeserializer() {
            return buf -> {
               short idx = buf.readShort();
               DimletType type = DimletType.values()[idx];
               String key = buf.readUtf(32767);
               DimletKey dimlet1 = new DimletKey(type, key);
               boolean craftable1 = buf.readBoolean();
               return new DimletClientHelper.DimletWithInfo(dimlet1, craftable1);
            };
         }

         public BiConsumer<RegistryFriendlyByteBuf, DimletClientHelper.DimletWithInfo> getSerializer() {
            return (buf, info) -> {
               DimletKey dimlet1 = info.dimlet();
               buf.writeShort(dimlet1.type().ordinal());
               buf.writeUtf(dimlet1.key());
               buf.writeBoolean(info.craftable());
            };
         }
      }
   }
}
