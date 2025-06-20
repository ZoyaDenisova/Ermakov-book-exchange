package org.bookswap.listings.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.mapper.UserMapper;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.entity.BookImage;
import org.bookswap.catalog.mapper.BookMapper;
import org.bookswap.catalog.repository.BookImageRepo;
import org.bookswap.listings.dto.ListingDto;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.entity.ListingImage;
import org.bookswap.listings.repository.ListingImageRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListingMapper {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final CityMapper cityMapper;

    private final ListingImageRepo listingImageRepo;
    private final BookImageRepo bookImageRepo;

    public ListingDto toDto(Listing listing) {
        Book book = listing.getBook();

        List<String> listingImageUrls = listingImageRepo.findByListingId(listing.getId()).stream()
                .map(ListingImage::getUrl)
                .toList();

        String bookImageUrl = bookImageRepo.findByBookId(book.getId()).stream()
                .map(BookImage::getUrl)
                .findFirst()
                .orElse(null);

        return new ListingDto(
                listing.getId(),
                bookMapper.toDto(book),
                cityMapper.toDto(listing.getCity()),
                userMapper.toDto(listing.getOwner()),
                listing.getCondition(),
                listingImageUrls,
                listing.isOpen(),
                listing.isBlocked(),
                listing.getCreatedAt()
        );
    }
}
