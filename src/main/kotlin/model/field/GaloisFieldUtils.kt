package model.field

import polynomials.BinaryPolynomial
import model.BinaryVector
import java.math.BigInteger

class GaloisFieldUtils {
    companion object {
        fun getAllIrreduciblePolynomialsWithDegree(degree: Int): List<BinaryPolynomial> {
            val result = ArrayList<BinaryPolynomial>()
            var i = BigInteger.ONE
            val limit = BinaryVector(BigInteger.TWO.pow(degree + 1), degree + 2).toBigInteger()
            while (i < limit) {
                val candidate =
                    BinaryPolynomial(BinaryVector(i, degree + 1).toBooleanArray())
                if (candidate.isIrreducible() && candidate.degree == degree) {
                    result.add(candidate)
                }
                i++
            }
            return result
        }
    }
}