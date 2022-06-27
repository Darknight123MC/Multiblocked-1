package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.trait.PneumaticMachineTrait;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.google.gson.*;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author youyihj
 */
public class PneumaticPressureCapability extends PneumaticBaseCapability<Float> {
    public static final PneumaticPressureCapability CAP = new PneumaticPressureCapability();

    protected PneumaticPressureCapability() {
        super("pneumatic_pressure", new Color(0xFF3C00).getRGB());
    }

    @Override
    public Float defaultContent() {
        return 2.0f;
    }

    @Override
    public Float copyInner(Float content) {
        return content;
    }

    @Override
    public CapabilityProxy<? extends Float> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new Proxy(tileEntity);
    }

    @Override
    public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.getAsFloat();
    }

    @Override
    public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public ContentWidget<? super Float> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("P", color)).setUnit("bar");
    }

    @Override
    public CapabilityTrait createTrait() {
        return new PneumaticMachineTrait(this);
    }

    private static class Proxy extends PneumaticBaseCapability.Proxy<Float> {
        public Proxy(TileEntity tileEntity) {
            super(CAP, tileEntity);
        }

        @Override
        protected List<Float> handleRecipeInner(IO io, Recipe recipe, List<Float> left, boolean simulate) {
            IAirHandler handler = getAirHandler();
            float sum = left.stream().reduce(0.0f, Float::sum);
            int consumeAir = (int) (sum * 50);
            if (handler != null && io == IO.IN) {
                int air = handler.getAir();
                if (Math.signum(air) == Math.signum(consumeAir) && Math.abs(air) >= Math.abs(consumeAir)) {
                    if (!simulate) {
                        handler.addAir(-consumeAir);
                    }
                    return null;
                }
            }
            return left;
        }
    }
}

