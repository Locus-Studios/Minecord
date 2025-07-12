

# Minecord

A lightweight Minecraft plugin that lets you privately message fellow players on Discord using in-game commands. Perfect for server communities that want seamless, secure, and in-context communication.


---

## Features

In-game private messaging: Easily message Discord users from Minecraft using /msgdc <username> <message>.

Two-way communication: Messages sent from Discord to Minecraft are forwarded to the appropriate in-game player.

Simple configuration: Quick setup with your Discord bot token and channel settings — no heavy dependencies.

Secure and private: Only targeted communication is relayed — no open chat leaks.



---

## Installation

1. Download the latest .jar from the releases page.


2. Place the .jar file into your proxy’s plugins/ directory.


3. Restart or reload your Proxy server.




---

## Configuration

A default config.yml will be created in plugins/Minecord/. Here’s what you need to set:

```
const DISCORD_BOT_TOKEN = 'YOUR_BOT_TOKEN';
const WS_URL = 'ws://localhost:8080';
const DISCORD_CHANNEL_ID = '';
const ALLOWED_ROLE_ID = 'ID_ALLOWED_ROLE';
```

bot-token: Your Discord bot’s token.

const WS_URL: Here you can config the port of websocket

const DISCORD_CHANNEL_ID: If you don't want to have logs of some messages leave it blank

const ALLOWED_ROLE_ID: Replace with your role

---

## Usage

In Minecraft

```/msgdc <discordUsername> <your message>```

Example:

/msgdc Player123 Hey, want to team up for the raid tonight?

In Discord

In the designated channel:

```!msgmc <MinecraftUsername> <your message>```

Example:

!msgmc Steve Sure! Meet in the nether portal area at 8 PM.


---

## Permissions

``minecord.use`` — Allows players to send Discord messages from Minecraft.


Add this to your permissions plugin (e.g., LuckPerms, PermissionsEx) to grant messaging capabilities.


---

### Supported Versions

Minecraft: 1.16 – 1.20

Java: 1.8+



---

### Integration Guide

1. Create a Discord Bot

Go to the Discord Developer Portal.

Create a new bot and copy its token.



2. Invite Bot to Your Server

Use an OAuth2 URL granting it at least the Send Messages permission.



3. Configure config.yml

Paste your bot token.

Set your preferred command prefix (default !).

Enter the Discord channel’s numeric ID.



4. (Optional) Role Restrictions

Use minecraft-role to restrict messaging to select Discord members.





---

### Support & Contribution

Bug reports, feature requests, and PRs are welcome!

Use the Issues tab to get in touch.



---

### License

Distributed under the MIT License. See LICENSE for full terms.


---

Made with ❤️ by Locus Studios — Bringing your Minecraft world and community closer together via Discord!

 
