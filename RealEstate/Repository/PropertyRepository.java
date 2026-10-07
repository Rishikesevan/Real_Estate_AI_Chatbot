package com.project.RealEstate.Repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.RealEstate.Entity.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

        List<Property> findByAgentId(Long id);

        List<Property> findTop8ByPropertyTypeOrderByIdDesc(String propertyType);

        List<Property> findTopByAgentId(Long agentId, PageRequest of);

        @Query("""
                        SELECT p FROM Property p
                        WHERE (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))
                        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                        AND (:propertyType IS NULL OR LOWER(p.propertyType) LIKE LOWER(CONCAT('%', :propertyType, '%')))
                        AND (:bhk IS NULL OR p.bhk = :bhk)
                        AND (:purpose IS NULL OR LOWER(p.buyRent) = LOWER(:purpose))
                        """)
        List<Property> searchProperties(
                        @Param("location") String location,
                        @Param("maxPrice") Double maxPrice,
                        @Param("propertyType") String propertyType,
                        @Param("bhk") Integer bhk,
                        @Param("purpose") String purpose);

        @Query("SELECT COALESCE(MIN(p.price),0) FROM Property p")
        Double findMinimumPrice();

        @Query("SELECT DISTINCT p.location FROM Property p WHERE LOWER(p.location) LIKE LOWER(CONCAT('%', :query, '%'))")
        List<String> findDistinctLocations(@Param("query") String query);

        @Query("SELECT DISTINCT p.propertyType FROM Property p WHERE LOWER(p.propertyType) LIKE LOWER(CONCAT('%', :query, '%'))")
        List<String> findDistinctPropertyTypes(@Param("query") String query);
}
