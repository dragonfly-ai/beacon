package beacon

import ai.dragonfly.uriel.ColorContext.sRGB.ARGB32
import narr.*

object StainedGlassSequence {

  def apply(serialized: NArray[ARGB32]):StainedGlassSequence = StainedGlassSequence(
    serialized(0),
    serialized.slice(1, serialized.length)
  )

}

case class StainedGlassSequence(approximateColor:ARGB32, sequence:NArray[ARGB32]) {
  def serialize:NArray[ARGB32] = {
    val out = NArray.ofSize[ARGB32](sequence.size + 1)
    out(0) = approximateColor
    NArray.copy[ARGB32](sequence, out, 1)
    out
  }
}