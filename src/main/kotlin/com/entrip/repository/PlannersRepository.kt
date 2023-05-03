package com.entrip.repository

import com.entrip.domain.entity.Planners
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PlannersRepository : JpaRepository<Planners, Long> {
    @Query("select p from Planners p left join fetch p.users u where p.planner_id = :planner_id")
    fun findPlannersByPlanner_idFetchUsers(@Param("planner_id") planner_id: Long): Optional<Planners>

    @Query("select p from Planners p left join fetch p.plans u where p.planner_id = :planner_id")
    fun findPlannersByPlanner_idFetchPlans(@Param("planner_id") planner_id: Long): Optional<Planners>

    @Query("select p from Planners p left join fetch p.notices u where p.planner_id = :planner_id")
    fun findPlannersByPlanner_idFetchNotices(@Param("planner_id") planner_id: Long): Optional<Planners>

    @Query("select p from Planners p left join fetch p.votes u where p.planner_id = :planner_id")
    fun findPlannersByPlanner_idFetchVotes(@Param("planner_id") planner_id: Long): Optional<Planners>

    @Query("select p from Planners p where p.planner_id = :planner_id")
    fun findPlannersByPlanner_idWithLazy(@Param("planner_id") planner_id: Long): Optional<Planners>
}