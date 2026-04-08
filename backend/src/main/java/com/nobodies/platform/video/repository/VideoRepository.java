package com.nobodies.platform.video.repository;

import com.nobodies.platform.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByDeletedFalse();

    List<Video> findByUploaderIdAndDeletedFalse(Long uploaderId);

    long countByUploaderIdAndDeletedFalse(Long uploaderId);

    @Query("select coalesce(sum(v.fileSize), 0) from Video v where v.deleted = false and v.uploaderId = :uploaderId")
    long sumFileSizeByUploaderId(@Param("uploaderId") Long uploaderId);

    @Query("select coalesce(sum(v.fileSize), 0) from Video v where v.deleted = false")
    long sumAllFileSize();

    long countByDeletedFalse();
}
