package beacon.message

import ai.dragonfly.uriel.ColorContext.sRGB.ARGB32
import beacon.StainedGlassSequence
import narr.*

object ResultsMessage {

  def apply(serialized: NArray[ARGB32]):ResultsMessage = ResultsMessage(
    serialized(0),
    StainedGlassSequence(serialized.tail)
  )

}

case class ResultsMessage(target:ARGB32, nearestMatch: StainedGlassSequence) {
  def serialize:NArray[ARGB32] = {
    val out = NArray.ofSize[ARGB32](nearestMatch.sequence.size + 2)
    out(0) = target
    out(1) = nearestMatch.approximateColor

    NArray.copy[ARGB32](nearestMatch.sequence, out, 2)
    out
  }
}
