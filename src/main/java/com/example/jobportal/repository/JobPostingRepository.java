package com.example.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.jobportal.model.JobPosting;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    @Query("select j from JobPosting j where j.active = true "
            + "and (:keyword = '' or lower(concat(j.title, ' ', j.company, ' ', j.requiredSkills)) like lower(concat('%', :keyword, '%'))) "
            + "and (:location = '' or lower(j.location) like lower(concat('%', :location, '%'))) "
            + "and (:employmentType = '' or lower(j.employmentType) = lower(:employmentType)) "
            + "order by j.postedAt desc")
    List<JobPosting> search(@Param("keyword") String keyword, @Param("location") String location,
                            @Param("employmentType") String employmentType);

    List<JobPosting> findAllByOrderByPostedAtDesc();
}