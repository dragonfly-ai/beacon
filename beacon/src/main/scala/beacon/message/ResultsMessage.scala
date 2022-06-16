package beacon.message

import ai.dragonfly.bitfrost.ColorContext.sRGB.ARGB32
import beacon.StainedGlassSequence
import bridge.array.*


object ResultsMessage {

  def apply(serialized: ARRAY[Int]):ResultsMessage = ResultsMessage(
    ARGB32(serialized.head),
    StainedGlassSequence(serialized.tail)
  )

}

case class ResultsMessage(target:ARGB32, nearestMatch: StainedGlassSequence) {
  def serialize:ARRAY[Int] = {
    val out = new ARRAY[Int](nearestMatch.sequence.size + 2)
    out(0) = target.argb
    out(1) = nearestMatch.approximateColor.argb
    var tail:List[ARGB32] = nearestMatch.sequence
    var i:Int = 2
    while (tail.nonEmpty) {
      out(i) = tail.head.argb
      tail = tail.tail
      i += 1
    }
    out
  }
}
