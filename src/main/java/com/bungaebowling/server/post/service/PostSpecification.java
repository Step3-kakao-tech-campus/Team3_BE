package com.bungaebowling.server.post.service;

import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.city.country.Country;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.user.User;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PostSpecification {

    public static final LocalDateTime START_TIME = LocalDateTime.now().minusMonths(3);

    public static Specification<Post> conditionEqual(String condition, Long userId) {
        return (root, query, criteriaBuilder) -> {
            Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
            Join<Post, Applicant> applicantJoin = root.join("applicants", JoinType.LEFT);
            Join<Applicant, User> applicantUserJoin = applicantJoin.join("user", JoinType.LEFT);
            Fetch<Post, District> districtFetch = root.fetch("district", JoinType.LEFT);
            Fetch<District, Country> countryFetch = districtFetch.fetch("country", JoinType.LEFT);
            countryFetch.fetch("city", JoinType.LEFT);
            root.fetch("applicants", JoinType.LEFT);

            Predicate createdPredicate = criteriaBuilder.equal(userJoin.get("id"), userId);
            Predicate participatedPredicate = criteriaBuilder.and(
                    criteriaBuilder.isTrue(applicantJoin.get("status")),
                    criteriaBuilder.equal(applicantUserJoin.get("id"), userId),
                    criteriaBuilder.notEqual(userJoin.get("id"), userId)
            );

            return switch (condition) {
                case "created" -> createdPredicate;
                case "participated" -> participatedPredicate;
                default -> criteriaBuilder.or(createdPredicate, participatedPredicate);
            };
        };
    }

    public static Specification<Post> statusEqual(String status) {
        return (root, query, criteriaBuilder) -> switch (status) {
            case "open" -> criteriaBuilder.equal(root.get("isClose"), false);
            case "closed" -> criteriaBuilder.equal(root.get("isClose"), true);
            default -> criteriaBuilder.conjunction();
        };
    }

    public static Specification<Post> cityIdEqual(Long cityId) {
        return (root, query, criteriaBuilder) -> {
            if (cityId != null) {
                return criteriaBuilder.equal(
                        root.join("district").join("country").join("city").get("id"),
                        cityId
                );
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Post> createdAtBetween(String start, String end) {
        return (root, query, criteriaBuilder) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime startDate = start == null ? START_TIME : LocalDate.parse(start, formatter).atStartOfDay();

            if (end == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), startDate);
            } else {
                LocalDateTime endDate = LocalDate.parse(end, formatter).plusDays(1).atStartOfDay();
                return criteriaBuilder.between(root.get("startTime"), startDate, endDate);
            }
        };
    }

    public static Specification<Post> postIdLessThan(Long postId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), postId);
    }
}
