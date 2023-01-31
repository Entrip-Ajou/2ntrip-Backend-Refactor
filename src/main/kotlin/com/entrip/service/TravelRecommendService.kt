package com.entrip.service

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class TravelRecommendService {

    private var logger: Logger = LoggerFactory.getLogger(TravelRecommendService::class.java)

    //private val localPath : String = "~~"
    private val EC2Path: String = "/home/ec2-user/app/step1/entrip-api-kotlin/resources/CollaborativeFiltering"
    private val localPath: String = "/Users/donghwan/Downloads/CollaborativeFiltering"

    fun callPython(users_id: String, region: String, recommendType: Int): String {
        val command: Array<String> =
            arrayOf("$EC2Path/venv/bin/python", "$EC2Path/main.py", users_id, region, recommendType.toString())
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