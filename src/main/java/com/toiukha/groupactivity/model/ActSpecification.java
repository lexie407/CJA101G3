package com.toiukha.groupactivity.model;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Utility class for building dynamic {@link Specification} for {@link ActVO}.
 */
public class ActSpecification {

    /**
     * Build a specification based on optional parameters.
     *
     * @param recruitStatus status of recruiting
     * @param actName       activity name contains
     * @param hostId        host id
     * @param isPublic      public flag
     * @param actStart      activity start time after or equal
     * @param maxCap        maximum capacity greater or equal
     * @return combined specification
     */
    public static Specification<ActVO> buildSpec(Byte recruitStatus,
                                                String actName,
                                                Integer hostId,
                                                Byte isPublic,
                                                LocalDateTime actStart,
                                                Integer maxCap) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (recruitStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("recruitStatus"), recruitStatus));
            }
            if (actName != null && !actName.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("actName"), "%" + actName + "%"));
            }
            if (hostId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("hostId"), hostId));
            }
            if (isPublic != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isPublic"), isPublic));
            }
            if (actStart != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("actStart"), actStart));
            }
            if (maxCap != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("maxCap"), maxCap));
            }
            return predicate;
        };
    }
}
