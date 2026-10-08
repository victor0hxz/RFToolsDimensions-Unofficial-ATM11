package mcjty.rftoolsdim.modules.essences.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record BiomeAbsorberData(Identifier biome, int absorbing) {
   public static final BiomeAbsorberData DEFAULT = new BiomeAbsorberData(null, 0);
   public static final Codec<BiomeAbsorberData> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("biome").forGetter(d -> Optional.ofNullable(d.biome)),
            Codec.INT.fieldOf("absorbing").forGetter(BiomeAbsorberData::absorbing)
         )
         .apply(inst, (biome, absorbing) -> new BiomeAbsorberData((Identifier)biome.orElse(null), absorbing))
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BiomeAbsorberData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(Identifier.STREAM_CODEC),
      d -> Optional.ofNullable(d.biome),
      ByteBufCodecs.INT,
      BiomeAbsorberData::absorbing,
      (biome, absorbing) -> new BiomeAbsorberData((Identifier)biome.orElse(null), absorbing)
   );
}
