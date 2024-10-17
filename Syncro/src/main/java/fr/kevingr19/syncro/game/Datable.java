package fr.kevingr19.syncro.game;

abstract public class Datable {

    public double heals = 0;
    public double damages = 0;
    public int deaths = 0;

    public double inflictedDamages = 0;
    public int kills = 0;
    public int suicides = 0;

    public double balance(){
        return heals - damages;
    }
    public String getBalanceText(){
        return "%s%.1f".formatted(balance() >= 0 ? "§2+" : "§4", balance());
    }

    public String[] getDataText(){
        return new String[]{
            "§f- Morts : §4%d§r §7(dont %d suicides)".formatted(deaths, suicides),
            "",
            "§f- Soins : §a%.1f".formatted(heals),
            "§f- Dégâts : §c%.1f".formatted(damages),
            "§f- Balance : %s".formatted(getBalanceText()),
            "",
            "§f- Dégâts infligés : §c%.1f".formatted(inflictedDamages),
            "§f- Kills : §4%d".formatted(kills)
        };
    }

}
