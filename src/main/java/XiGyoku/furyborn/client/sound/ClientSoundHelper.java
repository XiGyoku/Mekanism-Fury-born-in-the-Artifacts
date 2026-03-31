package XiGyoku.furyborn.client.sound;

import XiGyoku.furyborn.entity.RobyteEntity;
import XiGyoku.furyborn.sound.FuryBornSounds;
import net.minecraft.client.Minecraft;

public class ClientSoundHelper {
    public static void playRobyteBgm(RobyteEntity robyte) {
        Minecraft.getInstance().getSoundManager().play(
                new RobyteSoundInstance(robyte, FuryBornSounds.ROBYTE_BGM.get())
        );
    }
}