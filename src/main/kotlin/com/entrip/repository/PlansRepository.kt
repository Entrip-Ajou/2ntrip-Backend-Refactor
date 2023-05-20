package com.entrip.repository

import com.entrip.domain.entity.Plans
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PlansRepository : JpaRepository<Plans, Long> {
    @Query("select p from Plans p join fetch p.planners planner where p.plan_id = :plan_id")
    fun findPlansByPlan_idFetchPlanners(@Param("plan_id") plan_id: Long): Optional<Plans>

    @Query("select p from Plans p left join fetch p.comments c where p.plan_id = :plan_id")
    fun findPlansByPlan_idFetchComments(@Param("plan_id") plan_id: Long): Optional<Plans>
}