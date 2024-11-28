package net.blay09.mods.waystones.client.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class WaystoneModel extends Model {

    public WaystoneModel(ModelPart modelPart) {
        super(modelPart, it -> RenderType.cutout());
    }

    public static LayerDefinition createLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(144, 0).addBox(-10f, -48f, -10f, 20, 28, 20, cubeDeformation), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

}
