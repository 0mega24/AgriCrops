package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.*;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.environment.*;
import dev.omega24.agricrops.core.field.RootZone;
import dev.omega24.agricrops.core.genetics.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.*;
import java.util.*;

/** Player-local discoveries persist through death; raw alleles are never ordinary tooltips or sync tags. */
public final class FieldNotes {
    private FieldNotes() {}
    private static void say(EntityPlayer player, String key, Object... args) {
        TextFormatting color = key.equals("agricrops.analysis.paper") ? TextFormatting.GOLD : key.startsWith("agricrops.analysis") ? TextFormatting.AQUA : TextFormatting.GRAY;
        player.sendMessage(new TextComponentTranslation(key, args).setStyle(new Style().setColor(color)));
    }
    private static NBTTagCompound knowledge(EntityPlayer player) {
        NBTTagCompound entity = player.getEntityData();
        if (!entity.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) entity.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        NBTTagCompound persisted = entity.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persisted.hasKey("agricrops", 10)) persisted.setTag("agricrops", new NBTTagCompound()); return persisted.getCompoundTag("agricrops");
    }
    public static void probe(EntityPlayer player, CropTile tile) {
        player.sendMessage(tile.inspection());
        Environment environment = WorldEnvironment.sample(tile.getWorld(), tile.getPos()); RootZone roots = RootZones.read(tile.getWorld(), tile.getPos());
        for (String factor : Arrays.asList("temperature", "humidity", "air_quality", "height", "light", "rain", "sky")) {
            String id = "agricrops:" + factor;
            if (!environment.has(id)) { say(player, "agricrops.reason.factor_missing"); continue; }
            say(player, "agricrops.probe.factor", new TextComponentTranslation("agricrops.factor." + factor), String.format(Locale.ROOT, "%.2f", environment.get(id)), new TextComponentTranslation(environment.source(id)));
        }
        for (int i = 0; i < roots.cells.size(); i++) say(player, "agricrops.probe.layer", i + 1, roots.cells.get(i).block(), roots.cells.get(i).water(), roots.cells.get(i).nutrients(), roots.cells.get(i).organic());
        if (tile.crop() != null) {
            dev.omega24.agricrops.core.field.CropActions.Step status = dev.omega24.agricrops.core.field.CropActions.step(CropDefinitions.get(tile.crop().crop()), tile.crop(), roots, environment, CropDefinitions.all(), PocContent.traits());
            Phenotype phenotype = status.phenotype;
            say(player, "agricrops.probe.response", phenotype.reach, Math.round(phenotype.fit * 100), phenotype.limiting);
            if (!status.detail.isEmpty() && (status.reason == dev.omega24.agricrops.core.field.CropActions.Reason.ENVIRONMENT || status.reason == dev.omega24.agricrops.core.field.CropActions.Reason.FACTOR_MISSING
                    || status.reason == dev.omega24.agricrops.core.field.CropActions.Reason.FOUNDATION || status.reason == dev.omega24.agricrops.core.field.CropActions.Reason.SOIL || status.reason == dev.omega24.agricrops.core.field.CropActions.Reason.LIGHT)) say(player, "agricrops.probe.blocker", status.detail);
            knowledge(player).setBoolean("seen:" + tile.crop().crop(), true);
        }
    }
    public static void analyse(EntityPlayer player, CropTile tile) {
        if (tile.crop() == null || tile.quarantined()) { player.sendMessage(tile.inspection()); return; }
        if (!player.capabilities.isCreativeMode && player.inventory.clearMatchingItems(Items.PAPER, -1, 1, null) != 1) { say(player, "agricrops.analysis.paper"); return; }
        Specimen specimen = tile.crop().specimen(); NBTTagCompound knowledge = knowledge(player); knowledge.setBoolean("analysed:" + specimen.body, true);
        say(player, "agricrops.analysis.header", new TextComponentTranslation(CropDefinitions.get(specimen.body).nameKey()), new TextComponentTranslation("agricrops.origin." + specimen.origin.name().toLowerCase(Locale.ROOT)), specimen.generation);
        for (StandardStat stat : StandardStat.values()) say(player, "agricrops.analysis.gene", new TextComponentTranslation("agricrops.stat." + stat.id()), specimen.genes.get(stat));
        specimen.products.weights().forEach((id, weight) -> { knowledge.setBoolean("seen:" + id, true); say(player, "agricrops.analysis.component", new TextComponentTranslation(CropDefinitions.get(id).nameKey()), weight, specimen.products.total()); });
        specimen.traits.loci().forEach((id, pair) -> {
            if (!pair.first.isEmpty()) knowledge.setBoolean("trait:" + pair.first, true);
            if (!pair.second.isEmpty()) knowledge.setBoolean("trait:" + pair.second, true);
            say(player, "agricrops.analysis.trait", allele(pair.first), allele(pair.second), pair.fixed ? new TextComponentTranslation("agricrops.analysis.fixed") : new TextComponentTranslation("agricrops.analysis.open"), pair.generations);
        });
    }
    private static ITextComponent allele(String id) { return new TextComponentTranslation(id.isEmpty() ? "agricrops.analysis.empty" : PocContent.traits().get(id).nameKey); }
    public static void journal(EntityPlayer player) {
        NBTTagCompound knowledge = knowledge(player); say(player, "agricrops.journal.loop"); say(player, "agricrops.journal.recovery"); say(player, "agricrops.journal.supply");
        for (CropDefinition crop : CropDefinitions.all().values()) {
            say(player, "agricrops.journal.crop", new TextComponentTranslation(crop.nameKey()), crop.soil(), new TextComponentTranslation("agricrops.phase." + crop.rules().donor.name().toLowerCase(Locale.ROOT)), new TextComponentTranslation("agricrops.phase." + crop.rules().recipient.name().toLowerCase(Locale.ROOT)), crop.regrowthStage(), crop.rules().visual);
            for (Requirement rule : crop.rules().requirements) say(player, "agricrops.journal.requirement", rule.factor, rule.minimum, rule.maximum, rule.tolerance, rule.hard ? new TextComponentTranslation("agricrops.journal.hard") : new TextComponentTranslation("agricrops.journal.soft"));
            crop.rules().foundations.forEach((depth, block) -> say(player, "agricrops.journal.foundation", depth, block));
        }
        for (Breeding.Unlock unlock : PocContent.unlocks()) {
            if (knowledge.getBoolean("trait:" + unlock.trait)) say(player, "agricrops.journal.cross", new TextComponentTranslation(CropDefinitions.get(unlock.donor).nameKey()), new TextComponentTranslation(CropDefinitions.get(unlock.recipient).nameKey()), new TextComponentTranslation(PocContent.traits().get(unlock.trait).nameKey));
            else if (knowledge.getBoolean("seen:" + unlock.donor) && knowledge.getBoolean("seen:" + unlock.recipient)) say(player, "agricrops.journal.clue", new TextComponentTranslation(CropDefinitions.get(unlock.donor).nameKey()), new TextComponentTranslation(CropDefinitions.get(unlock.recipient).nameKey()));
        }
    }
}
