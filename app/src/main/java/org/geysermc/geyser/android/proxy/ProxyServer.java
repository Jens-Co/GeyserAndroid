/*
 * Copyright (c) 2020-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserAndroid
 */

package org.geysermc.geyser.android.proxy;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.v582.Bedrock_v582;

import org.geysermc.geyser.android.R;
import org.geysermc.geyser.android.utils.EventListeners;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ProxyServer {

    public static final BedrockPacketCodec CODEC = Bedrock_v582.V582_CODEC;

    private BedrockServer bdServer;
    private BedrockPong bdPong;

    public boolean shuttingDown = false;

    @SuppressLint("StaticFieldLeak")
    public static ProxyServer instance;

    public ProxyLogger proxyLogger;

    public ScheduledExecutorService generalThreadPool;

    public final Map<String, Player> players = new HashMap<>();

    public final String address;

    public final int port;

    public final Context ctx;

    public static final List<EventListeners.OnDisableEventListener> onDisableListeners = new ArrayList<>();

    public ProxyServer(String address, int port, Context ctx) {
        this.address = address;
        this.port = port;
        this.ctx = ctx;
    }

    public void onEnable() {
        instance = this;

        proxyLogger = new ProxyLogger();

        this.generalThreadPool = Executors.newScheduledThreadPool(32);

        // Start a timer to keep the thread running
        Timer timer = new Timer();
        TimerTask task = new TimerTask() { public void run() { } };
        timer.scheduleAtFixedRate(task, 0L, 1000L);

        // Initialise the palettes
        PaletteManger.init();

        start();
    }

    public void onDisable() {
        this.shutdown();

        for (EventListeners.OnDisableEventListener onDisableListener : onDisableListeners) {
            if (onDisableListener != null) onDisableListener.onDisable();
        }
    }

    private void start() {
        proxyLogger.info(ctx.getResources().getString(R.string.proxy_starting) + "...");

        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 19132);
        bdServer = new BedrockServer(bindAddress);


        bdPong = new BedrockPong();
        bdPong.setEdition("MCPE");
        bdPong.setMotd(ctx.getResources().getString(R.string.menu_proxy));
        bdPong.setSubMotd(ctx.getResources().getString(R.string.menu_proxy));
        bdPong.setPlayerCount(0);
        bdPong.setMaximumPlayerCount(1337);
        bdPong.setGameType("Survival");
        bdPong.setIpv4Port(19132);
        bdPong.setProtocolVersion(ProxyServer.CODEC.getProtocolVersion());
        bdPong.setVersion(ProxyServer.CODEC.getMinecraftVersion());

        bdServer.setHandler(new BedrockServerEventHandler() {
            @Override
            public boolean onConnectionRequest(@NonNull InetSocketAddress address) {
                return true; // Connection will be accepted
            }

            @Override
            public BedrockPong onQuery(@NonNull InetSocketAddress address) {
                return bdPong;
            }

            @Override
            public void onSessionCreation(@NonNull BedrockServerSession session) {
                session.setPacketHandler(new PacketHandler(session, instance));
            }
        });

        // Start server up
        bdServer.bind().join();
        proxyLogger.info(String.format(ctx.getResources().getString(R.string.proxy_started), "0.0.0.0:19132"));
    }

    public void shutdown() {
        proxyLogger.info(ctx.getResources().getString(R.string.proxy_shutdown));
        shuttingDown = true;

        bdServer.close();
        generalThreadPool.shutdown();
        instance = null;
        proxyLogger.info(ctx.getResources().getString(R.string.proxy_shutdown_done));
    }
}
