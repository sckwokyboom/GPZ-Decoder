import java.security.InvalidParameterException

object GaloisField {
    private val irreduciblePolynomial = BinaryPolynomial(intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1))
    private val primitiveElement = BinaryPolynomial(intArrayOf(0, 1))
    val fieldSize = 511
        get() = field
    val primitiveElementDegreeAndFieldElement = HashMap<Int, BinaryPolynomial>(fieldSize)
        get() = field
    val primitiveElementsAndInverses = HashMap<BinaryPolynomial, BinaryPolynomial>(fieldSize)
        get() = field

    init {
        for (i in 0 until fieldSize) {
            primitiveElementDegreeAndFieldElement[i] = primitiveElement.pow(i) % irreduciblePolynomial
        }
        for (i in 0 until fieldSize) {
            val element = primitiveElementDegreeAndFieldElement.getValue(i)
            if (primitiveElementsAndInverses.containsKey(element)) {
                continue
            }
//            if (primitiveElementsAndInverses.size == 2 * fieldSize) {
//                break
//            }
            for (j in i until fieldSize) {
                val candidate = primitiveElementDegreeAndFieldElement.getValue(j)
                if (primitiveElementsAndInverses.containsKey(candidate)) {
                    println("ПЛОХО!")
                }
                if ((element * candidate).castToFieldElements(this).isOne()) {
                    primitiveElementsAndInverses[element] = candidate
                    primitiveElementsAndInverses[candidate] = element
                    break
                }
            }
            if (!primitiveElementsAndInverses.containsKey(element)) {
                println("Обратного для $element нет!")
            }
        }

    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Степень примитивного элемента\t Элемент поля\n")
        for (entry in primitiveElementDegreeAndFieldElement) {
            builder.append("${entry.key} \t\t\t\t\t\t\t\t ${entry.value}\n")
        }
        return builder.toString()
    }

    operator fun get(index: Int): BinaryPolynomial {
        if (index > 510 || index < 0) {
            throw InvalidParameterException("Такая степень элемента в поле не ожидается: ${index}. Укажите число от 0 до 510 включительно.")
        }
        return primitiveElementDegreeAndFieldElement.getValue(index)
    }

    operator fun get(candidateToInverse: BinaryPolynomial): BinaryPolynomial {
        val fitToFieldCandidate = candidateToInverse.castToFieldElements(this)
        if (!primitiveElementsAndInverses.containsKey(fitToFieldCandidate)) {
            throw IllegalArgumentException("Такого элемента нет в поле: $fitToFieldCandidate")
        }
        return primitiveElementsAndInverses.getValue(fitToFieldCandidate)
    }
}