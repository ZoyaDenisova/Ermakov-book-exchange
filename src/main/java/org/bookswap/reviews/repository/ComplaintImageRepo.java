package org.bookswap.reviews.repository;

import org.bookswap.reviews.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintImageRepo extends JpaRepository<ComplaintImage, Long> {

    List<ComplaintImage> findByComplaintId(Long complaintId);
}
