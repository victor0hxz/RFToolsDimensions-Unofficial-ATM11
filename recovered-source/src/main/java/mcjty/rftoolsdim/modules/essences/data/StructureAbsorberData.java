package mcjty.rftoolsdim.modules.essences.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record StructureAbsorberData(Identifier structure, int absorbing) {
   public static final StructureAbsorberData DEFAULT = new StructureAbsorberData(null, 0);
   public static final Codec<StructureAbsorberData> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("structure").forGetter(d -> Optional.ofNullable(d.structure)),
            Codec.INT.fieldOf("absorbing").forGetter(StructureAbsorberData::absorbing)
         )
         .apply(inst, (structure, absorbing) -> new StructureAbsorberData((Identifier)structure.orElse(null), absorbing))
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, StructureAbsorberData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(Identifier.STREAM_CODEC),
      d -> Optional.ofNullable(d.structure),
      ByteBufCodecs.INT,
      StructureAbsorberData::absorbing,
      (structure, absorbing) -> new StructureAbsorberData((Identifier)structure.orElse(null), absorbing)
   );
}
