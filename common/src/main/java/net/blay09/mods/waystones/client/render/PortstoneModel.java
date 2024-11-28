package net.blay09.mods.waystones.client.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class PortstoneModel extends Model {

    public PortstoneModel(ModelPart modelPart) {
        super(modelPart, it -> RenderType.cutout());
    }

    public static LayerDefinition createLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-14f, 6f, -14f, 28, 52, 28, cubeDeformation), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

}
