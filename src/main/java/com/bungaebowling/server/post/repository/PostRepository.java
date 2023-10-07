package com.bungaebowling.server.post.repository;

import com.bungaebowling.server.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.id=:id")
    Optional<Post> findByIdJoinFetch(@Param("id") Long id);

    // 모집글들 조회
    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.id =:districtId ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdOrderByIdDesc(@Param("districtId") Long districtId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.id =:districtId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdAndIdLessThanOrderByIdDesc(@Param("districtId") Long districtId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.id =:countryId ORDER BY p.id DESC")
    List<Post> findAllByCountryIdOrderByIdDesc(@Param("countryId") Long countryId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.id =:countryId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCountryIdAndIdLessThanOrderByIdDesc(@Param("countryId") Long countryId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.city.id =:cityId ORDER BY p.id DESC")
    List<Post> findAllByCityIdOrderByIdDesc(@Param("cityId") Long cityId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.city.id =:cityId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCityIdAndIdLessThanOrderByIdDesc(@Param("cityId") Long cityId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "ORDER BY p.id DESC")
    List<Post> findAllOrderByIdDesc(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByIdLessThanOrderByIdDesc(@Param("key") Long key, Pageable pageable);

    // 마감안된 모집글만 조회
    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.id =:districtId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdWithCloseFalseOrderByIdDesc(@Param("districtId") Long districtId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.id =:districtId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("districtId") Long districtId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByCountryIdWithCloseFalseOrderByIdDesc(@Param("countryId") Long countryId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.id =:countryId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCountryIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("countryId") Long countryId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.city.id =:cityId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByCityIdWithCloseFalseOrderByIdDesc(@Param("cityId") Long cityId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.district.country.city.id =:cityId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCityIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("cityId") Long cityId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllWithCloseFalseOrderByIdDesc(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.user u JOIN FETCH p.district d JOIN FETCH d.country c JOIN FETCH c.city ci " +
            "WHERE p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByIdLessThanWithCloseFalseOrderByIdDesc(@Param("key") Long key, Pageable pageable);

    List<Post> findAllByUserIdAndIsCloseTrue(Long userId);

    List<Post> findAllByUserId(Long userId);
}
