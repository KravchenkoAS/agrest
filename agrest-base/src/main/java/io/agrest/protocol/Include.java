package io.agrest.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents 'include' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Include {

    private CayenneExp cayenneExp;
    private List<Sort> orderings;
    private String mapBy;
    private String path;
    private Integer start;
    private Integer limit;

    public Include(String path) {
        this(path, null, Collections.emptyList(), null, null, null);
    }

    public Include(
            String path,
            CayenneExp cayenneExp,
            List<Sort> orderings,
            String mapBy,
            Integer start,
            Integer limit) {

        this.path = Objects.requireNonNull(path);
        this.cayenneExp = cayenneExp;
        this.orderings = Objects.requireNonNull(orderings);
        this.mapBy = mapBy;
        this.start = start;
        this.limit = limit;
    }

    public String getMapBy() {
        return mapBy;
    }

    public String getPath() {
        return path;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLimit() {
        return limit;
    }

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public List<Sort> getOrderings() {
        return orderings;
    }

    @Override
    public String toString() {
        return "[Include:" + path + "]";
    }
}
