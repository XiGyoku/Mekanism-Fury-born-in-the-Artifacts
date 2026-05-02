package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import XiGyoku.furyborn.entity.RobixEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoadBikeBitModel extends GeoModel<RoadBikeBitEntity> {
    @Override
    public ResourceLocation getModelResource(RoadBikeBitEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "geo/roadbike_bit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoadBikeBitEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "textures/entity/roadbike_bit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoadBikeBitEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "animations/roadbike_bit.animation.json");
    }
}