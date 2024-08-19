package mateusu.acorrentados.InGame;

import mateusu.acorrentados.Acorrentados;
import mateusu.acorrentados.GameState;
import mateusu.acorrentados.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class Eventos implements Listener
{
    private Main plugin;
    private Acorrentados ac;
    private String prefix;

    public Eventos(Main plugin)
    {
        this.plugin = plugin;
        this.prefix = plugin.prefix;
        this.ac = plugin.getAcorrentados();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (ac.getState() == GameState.STARTED)
        {
            if (ac.getPlayersPlaying().contains(player.getUniqueId()))
            {
                if (player.getLocation().getBlock().getType() == Material.LAVA || player.getLocation().getBlock().getType() == Material.WATER ||
                        player.getLocation().getBlock().getType() == Material.BLACK_CONCRETE)
                {
                    if (ac.getCheckpoint() == null) return;

                    for (UUID playersId : ac.getPlayersPlaying()) {
                        Bukkit.getPlayer(playersId).teleport(ac.getCheckpoint());
                        Bukkit.getPlayer(playersId).sendMessage(prefix+ChatColor.RED + player.getName() + " morreu.");
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (ac.getState() == GameState.STARTED) {
            if (event.getClickedBlock() == null) return;
            if (event.getClickedBlock().getLocation() == ac.getCheckpoint()) return;

            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                Player player = event.getPlayer();
                Location location = event.getClickedBlock().getLocation();

                ac.setCheckpoint(location);
                player.sendMessage(prefix+ChatColor.GREEN + "Checkpoint salvo!");
            }
        }
    }
}
