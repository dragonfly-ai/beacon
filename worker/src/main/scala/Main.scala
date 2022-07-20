import ai.dragonfly.bitfrost.ColorContext.sRGB.{ARGB32, Lab}
import ai.dragonfly.bitfrost.color.model.rgb.discrete
import ai.dragonfly.math.squareInPlace
import ai.dragonfly.math.stats.DenseHistogramOfContinuousDistribution
import ai.dragonfly.math.stats.probability.distributions.stream.StreamingVectorStats
import ai.dragonfly.math.vector.*
import ai.dragonfly.spatial.PointRegionOctree
import beacon.StainedGlassSequence
import beacon.message.ResultsMessage
import bridge.array.*
import scalatags.Text.all.*

import java.io.{File, FileOutputStream, PrintWriter}
import scala.::
import scala.collection.mutable
import scala.language.postfixOps
import org.scalajs.dom
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.DedicatedWorkerGlobalScope.self as workerSelf

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}
import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

object Main extends App {

  private def log(s:String):Unit = println(s"Worker: $s")

  workerSelf.onmessage = (msg:MessageEvent) => {
    //log(s"Received ${msg.data}")
    msg.data match {
      case c:Int => makeBeacon(ARGB32(c))
      case "AWAKEN" => // postMessage("I'm awake!")
      case _ => log(s"unknown message ${msg.data}")
    }
  }

  inline private def postMessage(data: js.Any): Unit = workerSelf.postMessage(data)

  postMessage("I, the worker, have started!")

  log("Initializing ...")

  val dyeColors:ARRAY[ARGB32] = ARRAY[ARGB32](
    // primary:
    ARGB32(0xFF1D1D21), // Black
    ARGB32(0xFFB02E26), // Red
    ARGB32(0xFF5E7C16), // Green
    ARGB32(0xFF835432), // Brown
    ARGB32(0xFF3C44AA), // Blue
    ARGB32(0xFFFED83D), // Yellow
    ARGB32(0xFFF9FFFE), // White
    // secondary:
    ARGB32(0xFF8932B8), // Purple
    ARGB32(0xFF169C9C), // Cyan
    ARGB32(0xFF9D9D97), // Light gray
    ARGB32(0xFF474F52), // Gray
    ARGB32(0xFFF38BAA), // Pink
    ARGB32(0xFF80C71F), // Lime
    ARGB32(0xFF3AB3DA), // Light blue
    ARGB32(0xFFC74EBD), // Magenta
    ARGB32(0xFFF9801D) // Orange
  )

  val N:Int = 4928458

  var n:Int = 0

  def progress:Double = squareInPlace(n.toDouble / N.toDouble)
  var completed:Boolean = false

  val octree: PointRegionOctree[List[ARGB32]] = new PointRegionOctree[List[ARGB32]](128, Vector3(127.5, 127.5, 127.5), 16, 4, 10)

  val memoization: mutable.HashMap[ARGB32, List[ARGB32]] = mutable.HashMap[ARGB32, List[ARGB32]]()

  val bfsQ: mutable.Queue[StainedGlassSequence] = mutable.Queue[StainedGlassSequence]()

  for (dc <- dyeColors) { // add primary and secondary dye colors
    val path:List[ARGB32] = List[ARGB32](dc)
    memoization.put(dc, path)
    bfsQ.enqueue(StainedGlassSequence(dc, path))
  }

  log("Added primary colors.")
  val blockSize:Int = 100000

  var handle:SetTimeoutHandle = null

  var stopHerds:Boolean = false

  def populateOctree(): Unit = {
    stopHerds = true
    n = 0
    for ((c: ARGB32, path: List[ARGB32]) <- memoization) {
      n += 1
      octree.insert(Lab.toVector3(Lab.fromXYZ(c.toXYZ)), path)
      if (n % blockSize == 0) postMessage(ARRAY[js.Any]("STATUS", "OCTREE", progress))
    }
    completed = true
    postMessage(ARRAY[js.Any]("STATUS", "OCTREE", 1.0))
    postMessage(s"COMPLETED")
  }

  def nextPathBlock(): Unit = {
    if (completed) {
      postMessage(ARRAY[js.Any]("STATUS", "MEMOIZATION", 1.0))
      if (!stopHerds) populateOctree()
    } else {
      var bi: Int = 0
      while (bfsQ.nonEmpty && bi < blockSize) {
        val StainedGlassSequence(dc: ARGB32, path: List[ARGB32]) = bfsQ.dequeue()

        for (pc <- dyeColors) {

          val mix: ARGB32 = ARGB32.weightedAverage(dc, 0.5, pc, 0.5)

          memoization.get(mix) match {
            case Some(oldPath: List[ARGB32]) if oldPath.size <= path.size + 1 => // keep old path
            case _ =>
              val newPath: List[ARGB32] = pc :: path
              memoization.put(mix, newPath)
              bfsQ.enqueue(StainedGlassSequence(mix, newPath))
              n += 1
          }
        }
        bi += 1
      }

      if (bfsQ.isEmpty) completed = true
      else postMessage(ARRAY[js.Any]("STATUS", "MEMOIZATION", progress))

      handle = setTimeout(10) { nextPathBlock() }
    }
  }

  handle = setTimeout(10) { nextPathBlock() }

  def makeBeacon(target: ARGB32): Unit = {
    if (completed) {
      postMessage(
        (memoization.get(target) match {
          case Some(path: List[ARGB32]) => ResultsMessage(target, StainedGlassSequence(target, path))
          case None =>
            //log(s"memoization.size = ${memoization.size}")
            octree.nearestNeighbor(Lab.toVector3(Lab.fromXYZ(target.toXYZ))) match {
              case Some((nn: Vector3, path: List[ARGB32])) => ResultsMessage(target, StainedGlassSequence(ARGB32.fromXYZ(Lab.fromVector3(nn).toXYZ), path))
              case _ => throw Exception("empty octree or octree bug?")
            }
        }).serialize
      )
    } else {
      postMessage("Not ready yet!")
    }
  }

  log("Initialized!")

}
