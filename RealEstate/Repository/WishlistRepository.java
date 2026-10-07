package com.project.RealEstate.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.RealEstate.Entity.WishList;

public interface WishlistRepository extends JpaRepository<WishList, Long> 
{
    Optional<WishList> findByUserIdAndPropertyId(Long userId, Long propertyId);

    List<WishList> findByUserId(Long userId);
    
}
