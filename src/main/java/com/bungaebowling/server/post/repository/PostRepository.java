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

    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId")
    List<Post> findAllByDistrictId(@Param("districtId") Integer districtId);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId")
    List<Post> findAllByCountryId(@Param("countryId") Integer countryId);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId")
    List<Post> findAllByCityId(@Param("cityId") Integer cityId);

    // 모집글 마감 여부도 추가해서 조회
    @Query("SELECT p FROM Post p WHERE p.district.id =:districtId AND p.isClose = false")
    List<Post> findAllByDistrictIdWithCloseFalse(@Param("districtId") Integer districtId);

    @Query("SELECT p FROM Post p WHERE p.district.country.id =:countryId AND p.isClose = false")
    List<Post> findAllByCountryIdWithCloseFalse(@Param("countryId") Integer countryId);

    @Query("SELECT p FROM Post p WHERE p.district.country.city.id =:cityId AND p.isClose = false")
    List<Post> findAllByCityIdWithCloseFalse(@Param("cityId") Integer cityId);

    @Query("SELECT p FROM Post p WHERE p.isClose = false")
    List<Post> findAllWithCloseFalse();

    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findAllOrderByIdDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id < :key ORDER BY p.id DESC")
    List<Post> findAllByIdLessThanOrderByIdDesc(@Param("key") Long key, Pageable pageable);

}
