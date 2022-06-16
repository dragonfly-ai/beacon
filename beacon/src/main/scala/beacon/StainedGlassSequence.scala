package beacon

import ai.dragonfly.bitfrost.ColorContext.sRGB.ARGB32
import bridge.array.*

object StainedGlassSequence {

  def apply(serialized: ARRAY[Int]):StainedGlassSequence = StainedGlassSequence(
    ARGB32(serialized.head),
    List[ARGB32]() :++ serialized.tail.map((i:Int) => ARGB32(i))
  )

}

case class StainedGlassSequence(approximateColor:ARGB32, sequence:List[ARGB32]) {
  def serialize:ARRAY[Int] = {
    val out = new ARRAY[Int](sequence.size + 1)
    out(0) = approximateColor.argb
    var tail:List[ARGB32] = sequence
    var i:Int = 1
    while (tail.nonEmpty) {
      out(i) = tail.head.argb
      tail = tail.tail
      i += 1
    }
    out
  }
}