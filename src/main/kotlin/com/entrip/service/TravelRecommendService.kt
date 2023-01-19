package com.entrip.service

import com.entrip.socket.InfoWebSocketSessionStatsInfo
import com.google.common.collect.ImmutableList
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

    fun call_python() {
        val command: Array<String> = arrayOf("python3", "/Users/donghwan/pythonWorkSpace/main.py", "10", "20")
        try {
            exec_python(command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exec_python(command: Array<String>) {
        var commandLine: CommandLine = CommandLine.parse(command[0])

        for (i: Int in 1..command.size - 1)
            commandLine.addArgument(command[i])

        val outputStream = ByteArrayOutputStream()
        val pumpStreamHandler = PumpStreamHandler(outputStream)

        val executor = DefaultExecutor()
        executor.streamHandler = pumpStreamHandler

        var result: Int = executor.execute(commandLine)
        logger.info("result : $result")
        logger.info("output : ${outputStream.toString()}")

    }
}