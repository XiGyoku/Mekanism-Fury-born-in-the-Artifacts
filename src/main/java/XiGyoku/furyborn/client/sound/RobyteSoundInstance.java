package XiGyoku.furyborn.client.sound;

import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class RobyteSoundInstance extends AbstractTickableSoundInstance {
    private final RobyteEntity robyte;

    public RobyteSoundInstance(RobyteEntity robyte, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.robyte = robyte;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5F;
        this.pitch = 1.0F;
        this.x = robyte.getX();
        this.y = robyte.getY();
        this.z = robyte.getZ();
    }

    @Override
    public void tick() {
        if (this.robyte.isRemoved() || this.robyte.isDeadOrDying()) {
            this.stop();
        } else {
            this.x = this.robyte.getX();
            this.y = this.robyte.getY();
            this.z = this.robyte.getZ();
        }
    }
}