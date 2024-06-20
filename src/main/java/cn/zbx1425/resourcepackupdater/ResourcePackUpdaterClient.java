package cn.zbx1425.resourcepackupdater;

import cn.zbx1425.resourcepackupdater.drm.ServerLockRegistry;
import cn.zbx1425.resourcepackupdater.network.ClientVersionC2SPacket;
import cn.zbx1425.resourcepackupdater.network.ServerLockS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

public class ResourcePackUpdaterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> {
            ServerLockRegistry.onLoginInitiated();
        });
        ClientConfigurationNetworking.registerGlobalReceiver(ServerLockS2CPacket.TYPE, (packet, sender) -> {
            ServerLockRegistry.onSetServerLock(packet.serverLockKey);
            sender.sendPacket(new ClientVersionC2SPacket(ResourcePackUpdater.MOD_VERSION));
        });
        ClientConfigurationConnectionEvents.READY.register((handler, client) -> {
            ServerLockRegistry.onAfterSetServerLock();
        });
    }
}
