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
    
    /**
     * 複合查詢
     */
    public static Specification<ActVO> buildSpecWithRange(
            Byte recruitStatus,
            String actName,
            Integer hostId,
            Byte isPublic,
            LocalDateTime actStartFrom,    // 活動開始時間_從
            LocalDateTime actStartTo,      // 活動開始時間_到
            Integer maxCapMin,             // 最小人數需求
            Integer maxCapMax) {           // 最大人數需求
        
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
            
            // 範圍查詢：活動時間
            if (actStartFrom != null && actStartTo != null) {
                predicate = cb.and(predicate, cb.between(root.get("actStart"), actStartFrom, actStartTo));
            } else if (actStartFrom != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("actStart"), actStartFrom));
            } else if (actStartTo != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("actStart"), actStartTo));
            }
            
            // 範圍查詢：人數需求
            if (maxCapMin != null && maxCapMax != null) {
                predicate = cb.and(predicate, cb.between(root.get("maxCap"), maxCapMin, maxCapMax));
            } else if (maxCapMin != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("maxCap"), maxCapMin));
            } else if (maxCapMax != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("maxCap"), maxCapMax));
            }
            
            return predicate;
        };
    }
}
