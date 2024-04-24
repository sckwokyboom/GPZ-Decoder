package model

import model.code.Code

class Experimenter {

//    fun getData(code: Code, numOfIterations: Int, step: Double): ArrayList<Point> {
//        if (step > 1 || step <= 0) {
//            throw IllegalArgumentException("Шаг должен быть вещественным числом в интервале (0, 1].")
//        }
//        if (numOfIterations <= 0) {
//            throw IllegalArgumentException("Количество итераций должно быть целым положительным числом.")
//        }
//        val syndromeTableDecoder = SyndromeTableDecoder(code)
//        println(syndromeTableDecoder.syndromeTable.getTable())
//        var curChannelErrorProbability = 0.0
//        val results = ArrayList<Point>((1 / step).toInt() + 1)
//        while (curChannelErrorProbability <= 1) {
//            val channel = Channel(curChannelErrorProbability, code.length)
//            var incorrectlyDecodedVectors = 0;
//            for (i in 0 until numOfIterations) {
//                val vectorForSending = code.getRandomCodeword()
//                channel.send(vectorForSending)
//                val receivedVector = channel.receive()
//                val decodedVector = syndromeTableDecoder.decode(receivedVector)
//                if (vectorForSending != decodedVector) {
//                    incorrectlyDecodedVectors++
//                }
//            }
//            results.add(
//                Point(
//                    curChannelErrorProbability,
//                    incorrectlyDecodedVectors.toDouble() / numOfIterations
//                )
//            )
//            curChannelErrorProbability += step
//        }
//        return results
//    }
}