package com.bungaebowling.server.post.repository;

import com.bungaebowling.server.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdOrderByIdDesc(@Param("districtId") Integer districtId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdAndIdLessThanOrderByIdDesc(@Param("districtId") Integer districtId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId ORDER BY p.id DESC")
    List<Post> findAllByCountryIdOrderByIdDesc(@Param("countryId") Integer countryId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCountryIdAndIdLessThanOrderByIdDesc(@Param("countryId") Integer countryId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId ORDER BY p.id DESC")
    List<Post> findAllByCityIdOrderByIdDesc(@Param("cityId") Integer cityId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCityIdAndIdLessThanOrderByIdDesc(@Param("cityId") Integer cityId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findAllOrderByIdDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByIdLessThanOrderByIdDesc(@Param("key") Long key, Pageable pageable);

    // 마감안된 모집글만 조회
    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdWithCloseFalseOrderByIdDesc(@Param("districtId") Integer districtId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByDistrictIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("districtId") Integer districtId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByCountryIdWithCloseFalseOrderByIdDesc(@Param("countryId") Integer countryId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCountryIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("countryId") Integer countryId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId AND p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllByCityIdWithCloseFalseOrderByIdDesc(@Param("cityId") Integer cityId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId AND p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByCityIdAndIdLessThanWithCloseFalseOrderByIdDesc(@Param("cityId") Integer cityId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isClose = FALSE ORDER BY p.id DESC")
    List<Post> findAllWithCloseFalseOrderByIdDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isClose = FALSE AND p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByIdLessThanWithCloseFalseOrderByIdDesc(@Param("key") Long key, Pageable pageable);
}
