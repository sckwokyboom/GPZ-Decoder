package model.decoder

import model.BinaryVector

interface Decoder {
    fun decode(vector: BinaryVector): BinaryVector?
}