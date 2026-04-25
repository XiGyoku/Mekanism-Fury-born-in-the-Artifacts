package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.entity.RobixEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RobixModel extends GeoModel<RobixEntity> {
    @Override
    public ResourceLocation getModelResource(RobixEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "geo/robix.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RobixEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "textures/entity/robix.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RobixEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "animations/robix.animation.json");
    }
}