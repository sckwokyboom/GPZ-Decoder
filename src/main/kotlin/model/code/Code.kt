package model.code

import model.canonicalconverter.Matrix
import model.BinaryVector
import java.util.*
import java.util.stream.Stream
import kotlin.math.pow

abstract class Code {
    abstract val parityCheckLength: Int
    abstract val length: Int
    abstract val dimension: Int
    abstract val generatorMatrix: Matrix
    abstract val parityCheckMatrix: Matrix
    open fun getAllCodewords(): Stream<BinaryVector> {
        val countOfCodewords = (2.0.pow(dimension) - 1).toInt()
        val codeWords = HashSet<BinaryVector>()
        for (i in 0 until countOfCodewords + 1) {
            val basicVectors = BitSet(length)
            for (j in 0 until length) {
                if ((i and (1 shl j)) != 0) {
                    basicVectors.set(j)
                }
            }
            var codeWord = BinaryVector(length)
            basicVectors.stream().forEach { ind ->
                run {
                    codeWord += BinaryVector(generatorMatrix.row(ind))
                }
            }
            codeWords.add(codeWord)
        }
        return codeWords.stream()
    }

    open fun getRandomCodeword(): BinaryVector {
        val countOfCodewords = (2.0.pow(dimension) - 1).toInt()
        val randomCoefficients = Random().nextInt(countOfCodewords)

        val basicVectors = BitSet(dimension)
        for (j in 0 until dimension) {
            if ((randomCoefficients and (1 shl j)) != 0) {
                basicVectors.set(j)
            }
        }

        var codeWord = BinaryVector(length)
        basicVectors.stream().forEach { ind ->
            run {
                codeWord += BinaryVector(generatorMatrix.row(ind))
            }
        }
        return codeWord
    }
}