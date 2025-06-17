package org.bookswap.listings.repository;

import org.bookswap.listings.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingImageRepo extends JpaRepository<ListingImage, Long> {

    // Все изображения по объявлению
    List<ListingImage> findByListingId(Long listingId);

    // Удаление всех изображений при удалении листинга
    void deleteByListingId(Long listingId);
}
