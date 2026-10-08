package mcjty.rftoolsdim.modules.dimlets.data;

import net.minecraft.network.FriendlyByteBuf;

public record DimletKey(DimletType type, String key) implements Comparable<DimletKey> {
   public static DimletKey create(FriendlyByteBuf buf) {
      return new DimletKey(DimletType.values()[buf.readInt()], buf.readUtf(32767));
   }

   public static DimletKey create(String serialized) {
      String[] split = serialized.split("#");
      return new DimletKey(DimletType.byName(split[0]), split[1]);
   }

   public void toBytes(FriendlyByteBuf buf) {
      buf.writeInt(this.type.ordinal());
      buf.writeUtf(this.key);
   }

   public int compareTo(DimletKey dimletKey) {
      return dimletKey.type().equals(this.type) ? this.key.compareTo(dimletKey.key) : this.type.name().compareTo(dimletKey.type.name());
   }
}
