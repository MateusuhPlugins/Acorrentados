package mateusu.acorrentados;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Acorrentados
{
    private GameState state;

    private final List<UUID> playersPlaying = new ArrayList<>();
    private static final double MAX_DISTANCE = 6.0; // Distância máxima permitida entre jogadores

    private Location spawnLocation;
    private Location checkpoint;

    private Main plugin;
    private String prefix;

    public Acorrentados(Main plugin)
    {
        this.plugin = plugin;
        this.prefix = plugin.prefix;
    }

    private void connectPlayerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getState() == GameState.STARTED && !playersPlaying.isEmpty()) {
                    for (UUID playerId : playersPlaying) {
                        Player player = Bukkit.getPlayer(playerId);

                        if (playersPlaying.contains(player.getUniqueId())) {
                            checkPlayersDistance();

                            if (player.getLocation().getY() < 0) {
                                for (UUID playersId : playersPlaying) {
                                    Bukkit.getPlayer(playersId).sendMessage(prefix+ ChatColor.RED + player.getName() + " caiu do mapa.");
                                    Bukkit.getPlayer(playersId).teleport(getSpawnLocation());

                                    setCheckpoint(null);
                                }
                            }

                            // Verifica se o jogador está segurando Shift
                            if (player.isSneaking()) {
                                // Puxa os outros jogadores até o jogador que está agachado
                                for (UUID playersId : playersPlaying) {
                                    Player players = Bukkit.getPlayer(playersId);
                                    pullPlayerTo(player, players);
                                }
                            }
                        }
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 0L);
    }

    private void pullPlayerTo(Player target, Player playerToMove) {
        Location targetLocation = target.getLocation();
        Location playerLocation = playerToMove.getLocation();

        // Verifica a distância e ajusta a velocidade de puxar
        double distance = targetLocation.distance(playerLocation);
        if (distance > 1.0) { // Ajuste a distância mínima de puxar conforme necessário
            Vector direction = targetLocation.clone().subtract(playerLocation).toVector().normalize();
            double pullStrength = 0.1; // Força de puxar (ajuste conforme necessário)
            playerToMove.setVelocity(direction.multiply(pullStrength));
        } else {
            // Se o jogador estiver muito perto, teletransporta diretamente para evitar problemas de física
            playerToMove.teleport(targetLocation);
        }
    }

    public void checkPlayersDistance() {
        if (getState() == GameState.STARTED) {
            for (int i = 0; i < playersPlaying.size(); i++) {
                Player player = Bukkit.getPlayer(playersPlaying.get(i));
                if (player != null && player.isOnline()) {
                    for (int j = i + 1; j < playersPlaying.size(); j++) {
                        Player otherPlayer = Bukkit.getPlayer(playersPlaying.get(j));
                        if (otherPlayer != null && otherPlayer.isOnline()) {
                            double distance = player.getLocation().distance(otherPlayer.getLocation());
                            if (distance > MAX_DISTANCE) {
                                pullPlayersTogether(player, otherPlayer);
                            }
                            drawParticlesBetweenPlayers(player, otherPlayer);
                        }
                    }
                }
            }
        }
    }

    private void drawParticlesBetweenPlayers(Player player1, Player player2) {
        Location loc1 = player1.getLocation().clone().add(0, 1, 0); // Adiciona altura para que as partículas apareçam acima do solo
        Location loc2 = player2.getLocation().clone().add(0, 1, 0);

        double distance = loc1.distance(loc2);
        Vector vector = loc2.toVector().subtract(loc1.toVector()).normalize();

        // Definindo a cor das partículas como azul claro
        Particle.DustTransition dustTransition = new Particle.DustTransition(
                Color.fromRGB(64, 64, 64), // Start color
                Color.fromRGB(64, 64, 64), // End color
                1); // Size

        for (double i = 0; i < distance; i += 1.0) { // Aumenta o espaçamento entre as partículas
            Location point = loc1.clone().add(vector.clone().multiply(i));
            loc1.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, point, 1, dustTransition);
        }
    }

    private void pullPlayersTogether(Player player1, Player player2) {
        Location loc1 = player1.getLocation();
        Location loc2 = player2.getLocation();
        Vector direction = loc2.toVector().subtract(loc1.toVector()).normalize().multiply(0.05);

        player1.setVelocity(player1.getVelocity().add(direction));
        player2.setVelocity(player2.getVelocity().add(direction.multiply(-1)));
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public void setCheckpoint(Location checkpoint)
    {
        this.checkpoint = checkpoint;
    }

    public Location getCheckpoint()
    {
        return checkpoint;
    }

    public void addPlayer(Player player) {
        if(getState() == GameState.STARTED)
        {
            player.sendMessage(prefix+ ChatColor.RED + " Jogo ja iniciado.");
            return;
        }

        if (!playersPlaying.contains(player.getUniqueId())) {
            playersPlaying.add(player.getUniqueId());
            Bukkit.broadcastMessage(prefix+ChatColor.GREEN + player.getName() + " entrou no jogo.");
        } else {
            player.sendMessage(prefix+ChatColor.RED + "Você já está no jogo.");
        }
    }

    public void addAllPlayers() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if (!playersPlaying.contains(player.getUniqueId())) {
                playersPlaying.add(player.getUniqueId());
                Bukkit.broadcastMessage(prefix + ChatColor.GREEN + player.getName() + " entrou no jogo.");
            }
        }
    }

    public void removeAllPlayers()
    {
        Bukkit.broadcastMessage(prefix + ChatColor.RED + "Todos os jogadores foram removidos do jogo.");

        if(playersPlaying.isEmpty())
        {
            return;
        }

        playersPlaying.clear();
    }

    public void removePlayer(Player player) {
        if (playersPlaying.contains(player.getUniqueId())) {
            playersPlaying.remove(player.getUniqueId());
            Bukkit.broadcastMessage(prefix+ChatColor.RED + player.getName() + " saiu do jogo.");
        } else {
            player.sendMessage(prefix+ChatColor.RED + "Você não está no jogo.");
        }
    }

    public void setSpawnLocation(Location location) {
        plugin.getConfig().set("spawnLocation.world", location.getWorld().getName());
        plugin.getConfig().set("spawnLocation.x", location.getX());
        plugin.getConfig().set("spawnLocation.y", location.getY());
        plugin.getConfig().set("spawnLocation.z", location.getZ());
        plugin.getConfig().set("spawnLocation.yaw", location.getYaw());
        plugin.getConfig().set("spawnLocation.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public void startGame() {
        if(playersPlaying.size() < 2)
        {
            Bukkit.broadcastMessage(prefix+ChatColor.RED + "Acorrentados só pode iniciar com mais de uma pessoa.");
            return;
        }

        for(UUID playerId : playersPlaying)
        {
            Player player = Bukkit.getPlayer(playerId);
            player.teleport(getSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
        }

        connectPlayerTask();
        setState(GameState.STARTED);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Começou!");
    }

    public void stopGame()
    {
        setState(GameState.WAITING);
        setCheckpoint(null);

        for(UUID playerId : playersPlaying)
        {
            Player player = Bukkit.getPlayer(playerId);
            player.teleport(getSpawnLocation());
        }
        playersPlaying.clear();

        Bukkit.broadcastMessage(prefix+ChatColor.RED + "O jogo foi finalizado.");
    }

    public List<UUID> getPlayersPlaying() {
        return playersPlaying;
    }

    public Location getSpawnLocation() {
        FileConfiguration config = plugin.getConfig();
        String worldName = config.getString("spawnLocation.world");
        double x = config.getDouble("spawnLocation.x");
        double y = config.getDouble("spawnLocation.y");
        double z = config.getDouble("spawnLocation.z");
        float yaw = (float) config.getDouble("spawnLocation.yaw");
        float pitch = (float) config.getDouble("spawnLocation.pitch");

        if (worldName == null) {
            return null;
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

}
