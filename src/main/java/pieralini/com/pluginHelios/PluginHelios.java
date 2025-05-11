package pieralini.com.pluginHelios;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static su.nightexpress.coinsengine.api.CoinsEngineAPI.getCurrency;

public class PluginHelios extends JavaPlugin implements CommandExecutor, Listener {

    private static final List<String> CARGOS = Arrays.asList(
            "Forasteiro", "Plebeu", "Cidadão", "Burguês", "Aristocrata", "Nobre"
    );

    private CoinsEngineAPI coinsEngineAPI;

    @Override
    public void onEnable() {
        getCommand("cargos").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        // Verifique se o CoinsEngine está habilitado
        if (Bukkit.getPluginManager().isPluginEnabled("CoinsEngine")) {
            this.coinsEngineAPI = (CoinsEngineAPI) Bukkit.getPluginManager().getPlugin("CoinsEngine");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        String cargoAtual = "Cidadão"; // Defina o cargo atual, idealmente isto seria dinâmico
        int indexAtual = CARGOS.indexOf(cargoAtual);
        String cargoAnterior = (indexAtual > 0) ? CARGOS.get(indexAtual - 1) : null;
        String cargoProximo = (indexAtual < CARGOS.size() - 1) ? CARGOS.get(indexAtual + 1) : null;

        // Crie a GUI e abra para o jogador
        Inventory gui = criarGui(player, cargoAtual, cargoAnterior, cargoProximo);
        player.openInventory(gui);

        return true;
    }

    private Inventory criarGui(Player player, String cargoAtual, String cargoAnterior, String cargoProximo) {
        Inventory gui = Bukkit.createInventory(null, 45, "§6Cargos §- §fCargo: §a" + cargoAtual);

        preencherVidros(gui);

        // Botões de navegação entre cargos
        if (cargoAnterior != null) {
            gui.setItem(20, criarHeadCargo(cargoAnterior, "Anterior"));
        }

        gui.setItem(13, criarInfoPapel(player, cargoAtual)); // Info do jogador

        gui.setItem(22, criarHeadCargo(cargoAtual, "Atual")); // Cargo Atual

        if (cargoProximo != null) {
            gui.setItem(24, criarHeadCargo(cargoProximo, "Próximo"));
        }

        // Botão de recompensa
        gui.setItem(31, criarBotaoRecompensa());

        return gui;
    }

    private void preencherVidros(Inventory gui) {
        ItemStack vidro = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta vidroMeta = vidro.getItemMeta();
        vidroMeta.setDisplayName(" ");
        vidro.setItemMeta(vidroMeta);
        for (int i = 0; i < 45; i++) gui.setItem(i, vidro);
    }

    private ItemStack criarHeadCargo(String nomeCargo, String titulo) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName("§e" + titulo + ": §f" + nomeCargo);

        String preco = getConfig().getString("cargos." + nomeCargo + ".preco", "Indefinido");
        List<String> vantagens = getConfig().getStringList("cargos." + nomeCargo + ".vantagens");
        String descricao = getConfig().getString("cargos." + nomeCargo + ".descricao", "Sem descrição.");

        List<String> lore = new ArrayList<>();
        lore.add("§7Cargo: §f" + nomeCargo);
        lore.add("§7Preço: §f" + preco);
        lore.add("§7Vantagens:");
        for (String v : vantagens) lore.add(" §f- " + v);
        lore.add("§7Descrição: §f" + descricao);

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack criarInfoPapel(Player player, String cargoAtual) {
        ItemStack papel = new ItemStack(Material.PAPER);
        ItemMeta meta = papel.getItemMeta();
        meta.setDisplayName("§bInformações do Jogador");

        // Inicializa o saldo com um valor padrão
        double saldo = 0.0;

        if (player != null) {
            // Pegue a moeda com o ID ou nome correto
            Currency moeda = getCurrency("money");  // Passe o nome ou ID correto da moeda

            if (coinsEngineAPI != null && moeda != null) {
                saldo = coinsEngineAPI.getBalance(player, moeda);  // Obtém o saldo usando a moeda correta
            }
        }

        // Obtenha o nível do MCMMO usando PlaceholderAPI
        String nivelMCMMO = PlaceholderAPI.setPlaceholders(player, "%mcmmo_powerlevel%");

        DecimalFormat df = new DecimalFormat("#,##0.00");

        List<String> lore = new ArrayList<>();
        lore.add("§7Nome: §f" + player.getName());
        lore.add("§7Cargo: §f" + cargoAtual);
        lore.add("§7Nível MCMMO: §f" + nivelMCMMO);  // Use a placeholder para o nível
        lore.add("§7Coins: §f" + df.format(saldo));

        meta.setLore(lore);
        papel.setItemMeta(meta);
        return papel;
    }

    private ItemStack criarBotaoRecompensa() {
        ItemStack botao = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta botaoMeta = botao.getItemMeta();
        botaoMeta.setDisplayName("§aClique para receber sua recompensa");
        List<String> botaoLore = new ArrayList<>();
        botaoLore.add("§7Clique para executar os comandos de recompensa");
        botaoMeta.setLore(botaoLore);
        botao.setItemMeta(botaoMeta);
        return botao;
    }

    @EventHandler
    public void aoClicarInventario(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith("§6Cargos")) return;

        event.setCancelled(true);

        if (event.getSlot() == 31) {
            String cargoAtual = "Cidadão";
            List<String> comandos = getConfig().getStringList("cargos." + cargoAtual + ".comandos");

            for (String cmd : comandos) {
                String comandoFinal = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comandoFinal);
            }

            player.sendMessage("§aRecompensas de " + cargoAtual + " recebidas!");
            player.closeInventory();
        }
    }
}
