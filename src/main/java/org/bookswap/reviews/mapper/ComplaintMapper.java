package org.bookswap.reviews.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.mapper.UserMapper;
import org.bookswap.listings.mapper.ListingMapper;
import org.bookswap.reviews.dto.ComplaintDto;
import org.bookswap.reviews.entity.Complaint;
import org.bookswap.reviews.entity.ComplaintImage;
import org.bookswap.reviews.repository.ComplaintImageRepo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ComplaintMapper {

    private final ListingMapper listingMapper;
    private final UserMapper userMapper;
    private final ComplaintImageRepo complaintImageRepo;

    public ComplaintDto toDto(Complaint complaint) {
        return new ComplaintDto(
                complaint.getId(),
                listingMapper.toDto(complaint.getListing()),
                userMapper.toDto(complaint.getFromUser()),
                userMapper.toDto(complaint.getToUser()),
                complaint.getComment(),
                complaint.isReviewed(),
                complaintImageRepo.findByComplaintId(complaint.getId()).stream()
                        .map(ComplaintImage::getUrl)
                        .toList(),
                complaint.getCreatedAt()
        );
    }
}

