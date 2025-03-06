package keystrokesmod.mixin.impl.network;

import keystrokesmod.module.ModuleManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handlePlayerPosLook", at = @At("HEAD"))
    public void handlePlayerPosLookPre(S08PacketPlayerPosLook packetIn, CallbackInfo ci) {
        ModuleManager.noRotate.handlePlayerPosLookPre();
    }

    @Inject(method = "handlePlayerPosLook", at = @At("RETURN"))
    public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn, CallbackInfo ci) {
        ModuleManager.noRotate.handlePlayerPosLook(packetIn);
    }
}
