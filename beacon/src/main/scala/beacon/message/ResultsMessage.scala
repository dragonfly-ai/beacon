package beacon.message

import ai.dragonfly.bitfrost.ColorContext.sRGB.ARGB32
import beacon.StainedGlassSequence
import narr.*


object ResultsMessage {

  def apply(serialized: NArray[Int]):ResultsMessage = {
    val tail:NArray[Int] = NArray.tabulate[Int](serialized.length - 1)(i => serialized(i + 1))
    ResultsMessage(
      ARGB32(serialized(0)),
      StainedGlassSequence(tail)
    )
  }

}

case class ResultsMessage(target:ARGB32, nearestMatch: StainedGlassSequence) {
  def serialize:NArray[Int] = {
    val out = new NArray[Int](nearestMatch.sequence.size + 2)
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
