package cordori.dpstamina.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class StaminaChangeEvent extends Event implements Cancellable {

    private final Player player;
    @Setter private double cost;
    private boolean Cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public StaminaChangeEvent(Player player, double cost) {
        this.player = player;
        this.cost = cost;
    }

    @Override
    public HandlerList getHandlers() { return handlers; }

    @Override
    public boolean isCancelled() { return Cancelled; }

    @Override
    public void setCancelled(boolean Cancelled) {
        this.Cancelled = Cancelled;
    }


    public static StaminaChangeEvent callEvent(Player player, double cost) {
        StaminaChangeEvent event = new StaminaChangeEvent(player, cost);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
