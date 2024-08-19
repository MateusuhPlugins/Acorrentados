package mateusu.acorrentados;

import mateusu.acorrentados.InGame.Comandos;
import mateusu.acorrentados.InGame.Eventos;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Main  extends JavaPlugin {

    public String prefix = ChatColor.translateAlternateColorCodes('&', "&7Acorrentados &8&l> &r");
    public Acorrentados ac;

    @Override
    public void onEnable()
    {
        ac = new Acorrentados(this);
        ac.setState(GameState.WAITING);

        getCommand("ac").setExecutor(new Comandos(this));
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new Eventos(this), this);

        // Tarefa repetitiva para verificar a distância entre os jogadores
        new BukkitRunnable() {
            @Override
            public void run() {
                ac.checkPlayersDistance();
            }
        }.runTaskTimer(this, 0L, 20L); // Executa a cada segundo (20 ticks)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Acorrentados getAcorrentados()
    {
        return ac;
    }
}
