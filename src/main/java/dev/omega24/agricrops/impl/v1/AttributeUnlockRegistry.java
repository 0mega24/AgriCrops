package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.attribute.AttributeAllele;
import dev.omega24.agricrops.api.v1.attribute.AttributeUnlockRule;
import dev.omega24.agricrops.api.v1.attribute.IAttributeUnlockRegistry;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.attribute.LockedAttributeInstance;
import dev.omega24.agricrops.api.v1.misc.UnorderedPair;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default {@link IAttributeUnlockRegistry} implementation (crop-mod-design.md §7).
 */
public class AttributeUnlockRegistry implements IAttributeUnlockRegistry {

    private final Map<UnorderedPair<String>, List<AttributeUnlockRule>> rules;
    private final Set<UnorderedPair<String>> incompatible;

    public AttributeUnlockRegistry() {
        this.rules = new ConcurrentHashMap<>();
        this.incompatible = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public boolean addRule(@Nonnull String speciesA, @Nonnull String speciesB, @Nonnull String attributeId, double chance) {
        if (chance < 0.0 || chance > 1.0) {
            return false;
        }
        this.rules
                .computeIfAbsent(new UnorderedPair<>(speciesA, speciesB), k -> new CopyOnWriteArrayList<>())
                .add(new AttributeUnlockRule(attributeId, chance));
        return true;
    }

    @Nonnull
    @Override
    public List<AttributeUnlockRule> getRules(@Nonnull String speciesA, @Nonnull String speciesB) {
        return this.rules.getOrDefault(new UnorderedPair<>(speciesA, speciesB), Collections.emptyList());
    }

    @Override
    public boolean markIncompatible(@Nonnull String speciesA, @Nonnull String speciesB) {
        return this.incompatible.add(new UnorderedPair<>(speciesA, speciesB));
    }

    @Override
    public boolean isIncompatible(@Nonnull String speciesA, @Nonnull String speciesB) {
        return this.incompatible.contains(new UnorderedPair<>(speciesA, speciesB));
    }

    @Nonnull
    @Override
    public List<LockedAttributeInstance> rollCross(@Nonnull String speciesA, @Nullable IAgriStat parentA,
                                                     @Nonnull String speciesB, @Nullable IAgriStat parentB,
                                                     @Nonnull Random rand) {
        if (this.isIncompatible(speciesA, speciesB)) {
            return Collections.emptyList();
        }

        // Candidates: anything either parent already actively carries, plus any rule for this
        // pair that neither parent carries yet (each such rule gets its own independent
        // unlock roll — inheritance of an already-active attribute is not re-gated by chance).
        final Set<String> candidates = new LinkedHashSet<>();
        final Set<String> freshUnlocks = new HashSet<>();
        if (parentA != null) {
            parentA.getLockedAttributes().forEach(instance -> candidates.add(instance.getAttributeId()));
        }
        if (parentB != null) {
            parentB.getLockedAttributes().forEach(instance -> candidates.add(instance.getAttributeId()));
        }
        for (AttributeUnlockRule rule : this.getRules(speciesA, speciesB)) {
            final String attributeId = rule.getAttributeId();
            final boolean alreadyCarried = (parentA != null && parentA.getLockedAttribute(attributeId).isPresent())
                    || (parentB != null && parentB.getLockedAttribute(attributeId).isPresent());
            if (!alreadyCarried && rand.nextDouble() < rule.getChance()) {
                candidates.add(attributeId);
                freshUnlocks.add(attributeId);
            }
        }

        final List<LockedAttributeInstance> result = new ArrayList<>(candidates.size());
        for (String attributeId : candidates) {
            final Optional<LockedAttribute> definition = AgriApi.getAttributeRegistry().get(attributeId);
            if (!definition.isPresent()) {
                continue;
            }

            final Optional<LockedAttributeInstance> instanceA = parentA == null ? Optional.empty() : parentA.getLockedAttribute(attributeId);
            final Optional<LockedAttributeInstance> instanceB = parentB == null ? Optional.empty() : parentB.getLockedAttribute(attributeId);
            final boolean fresh = freshUnlocks.contains(attributeId);

            final AttributeAllele alleleFromA = instanceA
                    .map(instance -> instance.randomAllele(rand))
                    .orElseGet(() -> fresh ? rollFreshAllele(rand) : AttributeAllele.ABSENT);
            final AttributeAllele alleleFromB = instanceB
                    .map(instance -> instance.randomAllele(rand))
                    .orElseGet(() -> fresh ? rollFreshAllele(rand) : AttributeAllele.ABSENT);

            final int generations = (instanceA.isPresent() && instanceB.isPresent())
                    ? Math.min(instanceA.get().getGenerationsBredTrue(), instanceB.get().getGenerationsBredTrue()) + 1
                    : 0;

            result.add(new LockedAttributeInstance(attributeId, alleleFromA, alleleFromB, generations));
        }
        return result;
    }

    /**
     * A brand-new allele for an attribute that just got unlocked on this cross: a nonzero value
     * so the trait is actually visible/carryable, and a coinflip on dominance. Placeholder
     * balance numbers, like the rest of the stat/mutation math.
     */
    private static AttributeAllele rollFreshAllele(Random rand) {
        return new AttributeAllele(1 + rand.nextInt(CropStat.MAX), rand.nextBoolean());
    }

}
