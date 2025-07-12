package me.locusdevs.minecord;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(
        id = "minecord",
        name = "Minecord",
        version = "1.0-SNAPSHOT",
        description = "Plugin Minecraft <-> Discord bridge by LocusDevsTeam",
        authors = {"LocusDevsTeam"}
)
public class Main {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Inject
    private @com.velocitypowered.api.plugin.annotation.DataDirectory Path dataDirectory;

    private ConfigManager configManager;
    private WebSocketServer webSocketServer;
    private final Set<WebSocket> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            configManager = new ConfigManager(dataDirectory);
        } catch (Exception e) {
            logger.error("Error loading configuration!", e);
            return;
        }

        int port = configManager.getInt("websocket.port", 8080);

        webSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                connections.add(conn);
                logger.info("✅ WebSocket client connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                connections.remove(conn);
                logger.info("❌ WebSocket client disconnected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                logger.info("📩 Message received from Discord bot: " + message);

                String[] parts = message.split("\\|", 3);
                if (parts.length < 3) {
                    conn.send(formatMessage("messages.error-bad-format", null, null, null));
                    return;
                }

                String discordUser = parts[0];
                String targetPlayerName = parts[1];
                String content = parts[2];

                Player targetPlayer = server.getPlayer(targetPlayerName).orElse(null);

                if (targetPlayer != null && targetPlayer.isActive()) {
                    String formatted = formatMessage("messages.format-to-player", discordUser, targetPlayer.getUsername(), content);
                    targetPlayer.sendMessage(Component.text(formatted));

                    conn.send(formatMessage("messages.success-message-sent", null, targetPlayer.getUsername(), null));
                } else {
                    conn.send(formatMessage("messages.error-player-not-found", null, targetPlayerName, null));
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                logger.error("Error WebSocket", ex);
            }

            @Override
            public void onStart() {
                logger.info("🟢 WebSocket server started on port " + port);
            }
        };
        webSocketServer.start();

        CommandManager commandManager = server.getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("msgdc").build(),
                new SimpleCommand() {
                    @Override
                    public void execute(Invocation invocation) {
                        if (!(invocation.source() instanceof Player)) {
                            invocation.source().sendMessage(Component.text("This command can only be used in-game."));
                            return;
                        }

                        Player sender = (Player) invocation.source();

                        if (!sender.hasPermission("minecord.use")) { //name permission for use command
                            sender.sendMessage(Component.text(configManager.getString("messages.prefix-error", "❌") + " You do not have permission to use this command."));
                            return;
                        }

                        String[] args = invocation.arguments();

                        if (args.length < 2) {
                            sender.sendMessage(Component.text("Correct usage: /msgdc <DiscordUsername> <message>"));
                            return;
                        }

                        String discordUser = args[0];
                        String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

                        if (connections.isEmpty()) {
                            sender.sendMessage(Component.text(formatMessage("messages.error-no-discord-connected", null, null, null)));
                            return;
                        }

                        String payload = formatMessage("messages.format-to-discord", discordUser, sender.getUsername(), msg);

                        for (WebSocket conn : connections) {
                            conn.send(payload);
                        }

                        sender.sendMessage(Component.text(
                                formatMessage("messages.success-discord-message-sent", discordUser, null, null)
                        ));
                    }
                });

        logger.info("✅ Minecord started with configuration!");
    }

    private String formatMessage(String path, String discordUser, String minecraftUser, String message) {
        String raw = configManager.getString(path, "Missing message for: " + path);
        return raw
                .replace("%prefix-discord%", configManager.getString("messages.prefix-discord", "[Discord]"))
                .replace("%prefix-success%", configManager.getString("messages.prefix-success", "✅"))
                .replace("%prefix-error%", configManager.getString("messages.prefix-error", "❌"))
                .replace("%discordUser%", discordUser != null ? discordUser : "")
                .replace("%minecraftUser%", minecraftUser != null ? minecraftUser : "")
                .replace("%player%", minecraftUser != null ? minecraftUser : "")
                .replace("%message%", message != null ? message : "");
    }
}
