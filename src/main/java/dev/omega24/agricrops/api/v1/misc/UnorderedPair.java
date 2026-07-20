package dev.omega24.agricrops.api.v1.misc;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A pair of two values of the same type where {@code (a, b)} and {@code (b, a)} are considered
 * the same pair. Used to key species-pair lookups (crop-mod-design.md §7: cross-compatibility
 * and attribute-unlock rules don't care which species was planted on which side).
 *
 * @param <T> the type of both elements in the pair.
 */
public final class UnorderedPair<T> {

    private final T first;
    private final T second;

    public UnorderedPair(@Nonnull T first, @Nonnull T second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    @Nonnull
    public T getFirst() {
        return this.first;
    }

    @Nonnull
    public T getSecond() {
        return this.second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof UnorderedPair)) {
            return false;
        }
        final UnorderedPair<?> other = (UnorderedPair<?>) obj;
        return (this.first.equals(other.first) && this.second.equals(other.second))
                || (this.first.equals(other.second) && this.second.equals(other.first));
    }

    @Override
    public int hashCode() {
        // XOR keeps this symmetric under swapping first/second, matching equals().
        return this.first.hashCode() ^ this.second.hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }

}
