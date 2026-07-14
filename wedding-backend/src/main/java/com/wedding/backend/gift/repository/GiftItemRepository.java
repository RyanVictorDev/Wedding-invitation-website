package com.wedding.backend.gift.repository;

import com.wedding.backend.gift.model.GiftItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftItemRepository extends JpaRepository<GiftItem, Long> {

    List<GiftItem> findByActiveTrueOrderBySortOrderAscCreatedAtAsc();

    List<GiftItem> findAllByOrderBySortOrderAscCreatedAtAsc();
}
