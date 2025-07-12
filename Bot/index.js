const { Client, GatewayIntentBits } = require('discord.js');
const WebSocket = require('ws');

// 🔐 CONFIGURE THESE VALUES
const DISCORD_BOT_TOKEN = 'YOUR_BOT_TOKEN'; // <-- HERE YOU CAN ADD THE BOT TOKEN
const WS_URL = 'ws://localhost:8080'; // <-- HERE YOU CAN CONFIGURE THE WEBSOCKET PORT 
const DISCORD_CHANNEL_ID = ''; // <-- IF YOU DON'T WANT TO HAVE LOGS OF SOME MESSAGES LEAVE IT BLANK
const ALLOWED_ROLE_ID = 'ID_ALLOWED_ROLE'; // <-- REPLACE WITH YOUR ROLE

const client = new Client({
  intents: [
    GatewayIntentBits.Guilds,
    GatewayIntentBits.GuildMessages,
    GatewayIntentBits.MessageContent,
    GatewayIntentBits.DirectMessages,
  ],
  partials: ['CHANNEL'],
});

let ws;
let lastDiscordMessage = null;

function connectWebSocket() {
  ws = new WebSocket(WS_URL);

  ws.on('open', () => {
    console.log('✅ Connected to Minecraft Server WebSocket');
  });

  ws.on('message', async (data) => {
    const raw = data.toString();
    console.log('📩 Received from Minecraft:', raw);

    const parts = raw.split('|');
    if (parts.length !== 3) {
      console.warn('⚠️ Incorrect message format:', raw);
      return;
    }

    const sender = parts[0];
    const discordUserId = parts[1];
    const message = parts[2];

    if (/^\d{17,20}$/.test(discordUserId)) {
      try {
        const user = await client.users.fetch(discordUserId);
        await user.send(`📨 You have received a message from **${sender} (Minecraft)**:\n> ${message}`);
        console.log(`✅ DM sent to ${discordUserId}`);
      } catch (err) {
        console.error(`❌ Error sending DM to ${discordUserId}:`, err);
      }
    } else {
      const channel = client.channels.cache.get(DISCORD_CHANNEL_ID);
      if (channel) {
        channel.send(`📩 Message from **${sender}** to **${discordUserId}**: ${message}`);
      }

      if (lastDiscordMessage) {
        lastDiscordMessage.reply(`✅ Your message for **${discordUserId}** it has been forwarded.`);
        lastDiscordMessage = null;
      }
    }
  });

  ws.on('close', () => {
    console.log('❌ WebSocket disconnected. Reconnecting in 5 seconds...');
    setTimeout(connectWebSocket, 5000);
  });

  ws.on('error', (error) => {
    console.error('💥 Error WebSocket:', error);
  });
}

client.once('ready', () => {
  console.log(`🤖 Discord bot started as ${client.user.tag}`);
  connectWebSocket();
});

client.on('messageCreate', async (message) => {
  if (message.author.bot) return;

  if (message.content.startsWith('!msgmc')) {
    const member = await message.guild?.members.fetch(message.author.id);
    if (!member?.roles.cache.has(ALLOWED_ROLE_ID)) {
      return message.reply('❌ You do not have permission to use this command.');
    }

    const args = message.content.trim().split(/\s+/).slice(1);
    const targetMcUser = args.shift();
    const msg = args.join(' ');

    if (!targetMcUser || !msg) {
      return message.reply('❌ Correct usage: `!msgmc <MinecraftUsername> <message>`');
    }

    if (ws && ws.readyState === WebSocket.OPEN) {
      const payload = `${message.author.username}|${targetMcUser}|${msg}`;
      ws.send(payload);
      message.reply(`✅ Message sent to **${targetMcUser}**:\n> ${msg}`);
      lastDiscordMessage = message;
    } else {
      message.reply('❌ The WebSocket is not connected to the Minecraft server.');
    }
  }
});

client.login(DISCORD_BOT_TOKEN);
