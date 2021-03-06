package io.agrest.runtime.cayenne.processor.select;

import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.NestedDataResolverFactory;
import io.agrest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;

/**
 * @since 3.4
 */
public class CayenneNestedDataResolverBuilder {

    private ICayennePersister persister;

    public CayenneNestedDataResolverBuilder(ICayennePersister persister) {
        this.persister = persister;
    }

    public NestedDataResolverFactory viaQueryWithParentExp() {
        return this::viaQueryWithParentExp;
    }

    public NestedDataResolverFactory viaQueryWithParentIds() {
        return this::viaQueryWithParentIds;
    }

    // This will result in a JOINT prefetch on the parent. Note that DISJOINT prefetch is not available as an option,
    // as it is functionally equivalent to "viaQueryWithParentExp", and only complicates implementation without providing
    // a distinct useful alternative
    public NestedDataResolverFactory viaParentPrefetch() {
        return this::viaParentPrefetch;
    }

    protected NestedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaQueryWithParentExpResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    protected NestedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaQueryWithParentIdsResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    public NestedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaParentPrefetchResolver(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
    }

    protected void validateParent(Class<?> parentType, String relationshipName) {

        ObjEntity entity = persister.entityResolver().getObjEntity(parentType);

        if (entity == null) {
            throw new IllegalStateException("Entity '" + parentType.getSimpleName()
                    + "' is not mapped in Cayenne, so its relationship '"
                    + relationshipName
                    + "' can't be resolved with a Cayenne resolver");
        }

        if (entity.getRelationship(relationshipName) == null) {
            throw new IllegalStateException("Relationship '" + entity.getName() + "." + relationshipName
                    + "' is not mapped in Cayenne and can't be resolved with a Cayenne resolver");
        }
    }
}
