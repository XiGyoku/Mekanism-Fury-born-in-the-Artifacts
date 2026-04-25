package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RobixEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RobixRenderer extends GeoEntityRenderer<RobixEntity> {
    public RobixRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobixModel());
        this.shadowRadius = 0.5f;
    }
}