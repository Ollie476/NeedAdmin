package Admin.AdminSystem;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public record BlockSnapshot(Material type, BlockData data) {
}