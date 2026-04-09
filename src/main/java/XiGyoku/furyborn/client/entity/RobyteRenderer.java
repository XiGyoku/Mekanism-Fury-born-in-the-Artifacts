package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RobyteRenderer extends GeoEntityRenderer<RobyteEntity> {
    public RobyteRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobyteModel());
        this.shadowRadius = 0.5f;
        this.withScale(2.0f);
        this.addRenderLayer(new RobyteHaloLayer(this));
        this.addRenderLayer(new RobyteAfterImageLayer(this));
    }

    @Override
    protected float getDeathMaxRotation(RobyteEntity entity) {
        return 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(RobyteEntity entity) {
        return new ResourceLocation(Furyborn.MODID, "textures/entity/robyte_texture.png");
    }

    @Override
    public Color getRenderColor(RobyteEntity animatable, float partialTick, int packedLight) {
        if (animatable.isTransamMode() || animatable.isRebellion()) {
            return Color.ofRGBA(255, 180, 180, 255);
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }
}