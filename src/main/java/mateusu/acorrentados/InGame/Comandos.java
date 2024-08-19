package mateusu.acorrentados.InGame;

import mateusu.acorrentados.Acorrentados;
import mateusu.acorrentados.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comandos implements CommandExecutor
{
    private Main plugin;
    private String prefix;
    private Acorrentados ac;

    public Comandos(Main plugin)
    {
        this.plugin = plugin;
        this.prefix = plugin.prefix;
        this.ac = plugin.getAcorrentados();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (args.length == 0)
        {
            sender.sendMessage(prefix+ ChatColor.RED + "Uso: /ac <enter|setspawn|start>");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            switch (args[0].toLowerCase()) {
                case "enter":
                    ac.addPlayer(player);
                    break;

                case "leave":
                    ac.removePlayer(player);
                    break;

                case "addall":
                    ac.addAllPlayers();
                    break;

                case "removeall":
                    ac.removeAllPlayers();
                    break;

                case "setspawn":
                    ac.setSpawnLocation(player.getLocation());
                    player.sendMessage(prefix+ChatColor.GREEN + "Local de spawn definido.");
                    break;

                case "start":
                    ac.startGame();
                    break;

                case "stop":
                    ac.stopGame();
                    break;

                default:
                    player.sendMessage(prefix+ChatColor.RED + "Comando desconhecido.");
                    break;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");
        }

        return false;
    }
}
