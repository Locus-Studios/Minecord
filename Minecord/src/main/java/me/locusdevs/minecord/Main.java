package me.locusdevs.minecord;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

public final class Main extends JavaPlugin {

    private WebSocketServer webSocketServer;

    @Override
    public void onEnable() {

        webSocketServer = new WebSocketServer(new InetSocketAddress(8080)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                getLogger().info("WebSocket open: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                getLogger().info("WebSocket closed: " + reason);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                Bukkit.getScheduler().runTask(Main.this, () -> {
                    Bukkit.broadcastMessage("§b[Discord] §f" + message);
                });
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                getLogger().warning("Errore WebSocket: " + ex.getMessage());
            }

            @Override
            public void onStart() {
                getLogger().info("WebSocket started on port 8080");
            }
        };

        webSocketServer.start();
    }

    @Override
    public void onDisable() {
        try {
            if (webSocketServer != null) {
                webSocketServer.stop();
                getLogger().info("WebSocket server closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
