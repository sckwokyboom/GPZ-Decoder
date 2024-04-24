package model.field

import polynomials.BinaryPolynomial
import java.security.InvalidParameterException

class GaloisFieldMod2(
    private val irreduciblePolynomial: BinaryPolynomial,
    private val primitiveElement: BinaryPolynomial,
    val fieldSize: Int,
) {
    val primitiveElementDegreeAndFieldElement = HashMap<Int, BinaryPolynomial>(fieldSize)
    private val primitiveElementsAndInverses = HashMap<BinaryPolynomial, BinaryPolynomial>(fieldSize)

    init {
        if (!irreduciblePolynomial.isIrreducible()) {
            throw IllegalArgumentException("Многочлен $irreduciblePolynomial не является неприводимым над полем Галуа порядка 2.")
        }
        fillPrimitiveElementDegreesAndFieldElementsTable()
        fillInverseElementsTable()
    }

    private fun fillPrimitiveElementDegreesAndFieldElementsTable() {
        for (i in 0 until fieldSize) {
            val fieldElement = primitiveElement.pow(i) % irreduciblePolynomial
            if (primitiveElementDegreeAndFieldElement.containsValue(fieldElement)) {
                throw IllegalArgumentException("По указанному неприводимому многочлену $irreduciblePolynomial и примитивному элементу $primitiveElement невозможно построить поле Галуа размера ${fieldSize}. Некорректный размер или указанный элемент не является примитивным.")
            }
            primitiveElementDegreeAndFieldElement[i] = fieldElement
        }
    }

    private fun fillInverseElementsTable() {
        for (i in 0 until fieldSize) {
            val element = primitiveElementDegreeAndFieldElement.getValue(i)
            if (primitiveElementsAndInverses.containsKey(element)) {
                continue
            }
            if (primitiveElementsAndInverses.size == fieldSize) {
                break
            }
            for (j in i until fieldSize) {
                val candidate = primitiveElementDegreeAndFieldElement.getValue(j)
                if (primitiveElementsAndInverses.containsKey(candidate)) {
                    println("ПЛОХО! ${candidate}")
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

    fun findDegreeByFieldElement(fieldElement: BinaryPolynomial): Int {
        for (entry in primitiveElementDegreeAndFieldElement) {
            if (entry.value == fieldElement.castToFieldElements(this)) {
                return entry.key
            }
        }
        throw IllegalArgumentException("Элемента $fieldElement в поле нет.")
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