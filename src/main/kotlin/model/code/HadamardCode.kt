package model.code

import model.canonicalconverter.CanonicalConverter
import model.canonicalconverter.Matrix
import model.BinaryVector
import java.util.*
import java.util.stream.Stream
import kotlin.math.pow

class HadamardCode(override val dimension: Int) : Code() {
    companion object {
        private fun generateHammingParityCheckMatrix(codeDimension: Int): Matrix {
            val codeLength = (2.0.pow(codeDimension) - 1).toInt()
            val parityCheckMatrix = Matrix(codeDimension, codeLength)
            for (i in 0 until codeLength) {
                val binaryString = Integer.toBinaryString(i + 1).padStart(codeDimension, '0')
                for (j in 0 until codeDimension) {
                    parityCheckMatrix[j, i] = binaryString[j].code.toByte() - '0'.code.toByte()
                }
            }
            return parityCheckMatrix
        }
    }

    override val length: Int = (2.0.pow(dimension) - 1).toInt()
    override val generatorMatrix: Matrix = generateHammingParityCheckMatrix(dimension)
    override val parityCheckMatrix: Matrix
    override val parityCheckLength: Int = length - dimension

    init {
        val convertor = CanonicalConverter()
        parityCheckMatrix = convertor.toParityCheck(generatorMatrix)
    }
}