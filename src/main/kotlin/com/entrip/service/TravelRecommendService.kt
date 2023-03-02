package com.entrip.service

import com.entrip.domain.dto.TravelRecommend.TravelRecommendResponseDto
import com.entrip.domain.dto.UsersTravelFavorite.TravelFavoriteDtoComparator
import com.entrip.domain.entity.TravelFavorite
import com.entrip.domain.entity.UsersTravelFavorites
import com.entrip.repository.UsersTravelFavoritesRepository
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Collections

@Service
class TravelRecommendService(
    private final val usersTravelFavoritesRepository : UsersTravelFavoritesRepository
) {
    private var logger: Logger = LoggerFactory.getLogger(TravelRecommendService::class.java)

    //private val localPath : String = "~~"
    private val EC2Path: String = "/home/ec2-user/app/step1/entrip-api-kotlin/src/main/resources/CollaborativeFiltering"
    private val localPath: String = "/Users/donghwan/Downloads/CollaborativeFiltering"

    fun callPython(user_id : String): TravelRecommendResponseDto {
        val region : String = getHighScoreRegion(user_id)
        val recommendRegions : MutableList<String> = ArrayList()

        logger.info("High Score Resion is $region")

        recommendRegions.add(getModelResult(user_id, region, 1))
        recommendRegions.add(getModelResult(user_id, region, 2))
        recommendRegions.add(getModelResult(user_id, region, 3))

        return TravelRecommendResponseDto(user_id = user_id, recommendRegions = recommendRegions)
    }

    // 정렬해서 가장 높은 점수를 받은 지역 하나 추출
    private fun getHighScoreRegion(user_id: String): String {
        val travelFavoriteSet : MutableSet<TravelFavorite> = findSetById(user_id)
        val travelFavoriteList : MutableList<TravelFavorite> = ArrayList(travelFavoriteSet)

        Collections.sort(travelFavoriteList, TravelFavoriteDtoComparator())

        return travelFavoriteList[0].region
    }

    fun findSetById(user_id : String) : MutableSet<TravelFavorite> {
        val usersTravelFavorite : UsersTravelFavorites = usersTravelFavoritesRepository.findById(user_id).orElseThrow {
            IllegalArgumentException("Error raise at usersTravelFavoritesRepository.findById$user_id")
        }

        return usersTravelFavorite.travelFavorite_set
    }

    fun getModelResult(user_id: String, region: String, recommendType : Int) : String {
        val command: Array<String> =
            arrayOf("python3", "$EC2Path/main.py", user_id, region, recommendType.toString())
        var result: String = ""
        try {
            result = execPython(command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun execPython(command: Array<String>): String {
        val commandLine: CommandLine = CommandLine.parse(command[0])


        for (i: Int in 1 until command.size)
            commandLine.addArgument(command[i], false)

        val outputStream = ByteArrayOutputStream()
        val pumpStreamHandler = PumpStreamHandler(outputStream)

        val executor = DefaultExecutor()
        executor.streamHandler = pumpStreamHandler

        executor.execute(commandLine)
        //var result: Int = executor.execute(commandLine)
        //logger.info("result : $result")
        val result: String = outputStream.toString()
        logger.info("output : $result")
        return result
    }
}