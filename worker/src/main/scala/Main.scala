import ai.dragonfly.uriel.ColorContext.sRGB.{ARGB32, Luv}
import slash.squareInPlace
import slash.vector.*
import ai.dragonfly.spatial.PROctree
import beacon.StainedGlassSequence
import beacon.message.ResultsMessage
import narr.*
import scalatags.Text.all.*

import scala.collection.mutable
import scala.language.implicitConversions
import scala.language.postfixOps
import org.scalajs.dom
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.DedicatedWorkerGlobalScope.self as workerSelf

import scala.scalajs.js
import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

object Main extends App {

  given Conversion[ARGB32, Vec[3]] with
    inline def apply(argb: ARGB32): Vec[3] = Luv.toVec(Luv.fromXYZ(argb.toXYZ))

  given Conversion[Vec[3], ARGB32] with
    inline def apply(v:Vec[3]): ARGB32 = ARGB32.fromRGB(Luv.fromVec(v).toRGB)

  given Conversion[IntArray, NArray[ARGB32]] with
    inline def apply(ia: IntArray): NArray[ARGB32] = ia.asInstanceOf[NArray[ARGB32]]

  given Conversion[NArray[ARGB32], IntArray] with
    inline def apply(ca: NArray[ARGB32]): IntArray = ca.asInstanceOf[IntArray]

  private def log(s:String):Unit = println(s"Worker: $s")

  workerSelf.onmessage = (msg:MessageEvent) => {
    log(s"Received ${msg.data}")
    msg.data match {
      case c:Int => makeBeacon(ARGB32(c))
      case "AWAKEN" => // postMessage("I'm awake!")
      case _ => log(s"unknown message ${msg.data}")
    }
  }

  inline private def postMessage(data: js.Any): Unit = workerSelf.postMessage(data)

  postMessage("Ready to work!")

  log("Initializing ...")

  val dyeColors:NArray[ARGB32] = NArray[ARGB32](
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

  def progress:Double = squareInPlace( n.toDouble / N.toDouble )
  var completed:Boolean = false

  //val octree: PROctreeMap[IntArray] = new PROctreeMap[IntArray]( 128, Vec[3](127.5, 127.5, 127.5), 64 )
  val octree: PROctree = new PROctree( 400, Vec[3](9.565747360311072, -0.9330505612074277, 60.28835951296638), 64 )

  val memoization: mutable.HashMap[ARGB32, NArray[ARGB32]] = mutable.HashMap[ARGB32, NArray[ARGB32]]()

  val bfsQ: mutable.Queue[StainedGlassSequence] = {
    val tQ: mutable.Queue[StainedGlassSequence] = mutable.Queue[StainedGlassSequence]()

    var i: Int = 0; while (i < dyeColors.length) { // add primary and secondary dye colors
      val dc: ARGB32 = dyeColors(i)
      val path: NArray[ARGB32] = NArray.fill[ARGB32](1)(dc)
      memoization.put(dc, path)
      tQ.enqueue(StainedGlassSequence(dc, path))
      i += 1
    }
    tQ
  }

  var maxPathLength = 0

  log("Added primary colors.")
  val blockSize:Int = 100000

  var handle:SetTimeoutHandle = null

  var stopHerds:Boolean = false

  def populateOctree(): Unit = {
    stopHerds = true
    n = 0
    for ((c: ARGB32, path: NArray[ARGB32]) <- memoization) {
      n += 1
      octree.insert(c)
      if (n % blockSize == 0) postMessage(NArray[js.Any]("STATUS", "OCTREE", progress))
    }
    completed = true
    postMessage(NArray[js.Any]("STATUS", "OCTREE", 1.0))
    postMessage(s"COMPLETED")
    log(s"octree.size = ${octree.size}")
  }

  def nextPathBlock(): Unit = {
    if (completed) {
      postMessage(NArray[js.Any]("STATUS", "MEMOIZATION", 1.0))
      if (!stopHerds) populateOctree()
    } else {
      var bi: Int = 0
      while (bfsQ.nonEmpty && bi < blockSize) {
        val StainedGlassSequence(dc: ARGB32, path: NArray[ARGB32]) = bfsQ.dequeue()

        var i:Int = 0; while (i < dyeColors.length) {

          val pc:ARGB32 = dyeColors(i)
          val mix:ARGB32 = ARGB32.weightedAverage(dc, 0.5, pc, 0.5)

          memoization.get(mix) match {
            case Some(oldPath: IntArray) if oldPath.size <= path.size + 1 => // keep old path
            case _ =>
              val newPath: IntArray = NArray.ofSize[Int](1 + path.size)
              newPath(0) = pc
              NArray.copy[Int](path, newPath, 1)
              memoization.put(mix, newPath)
              if (newPath.length > maxPathLength) {
                maxPathLength = newPath.length
                log(s"New Max Path Length: $maxPathLength")
              }
              bfsQ.enqueue(StainedGlassSequence(mix, newPath))
              n += 1
          }
          i += 1
        }
        bi += 1
      }

      if (bfsQ.isEmpty) completed = true
      else postMessage(NArray[js.Any]("STATUS", "MEMOIZATION", progress))

      handle = setTimeout(10) { nextPathBlock() }
    }
  }

  handle = setTimeout(10) { nextPathBlock() }

  def makeBeacon(target: ARGB32): Unit = {
    if (completed) {
      log(s"makeBeacon(${target.render})")

      postMessage(
        (memoization.get(target) match {
          case Some(path: IntArray) =>
            log(s"Exact match: $path")
            ResultsMessage(target, StainedGlassSequence(target, path))
          case _ =>
            log(s"memoization.size = ${memoization.size}")
            octree.nearestNeighbor(Luv.toVec(Luv.fromXYZ(target.toXYZ))) match {
              case (nn: Vec[3]) =>
                val c: ARGB32 = nn
                memoization.get(c) match {
                  case Some(path: NArray[ARGB32]) =>
                    log(s"${nn.render()}, $path")
                    // FF7DD3
                    val sgs = StainedGlassSequence(ARGB32.fromXYZ(Luv.fromVec(nn).toXYZ), path)
                    log(s"StainedGlassSequence(ARGB32.fromXYZ(Luv.fromVec(nn).toXYZ), path) $sgs")
                    ResultsMessage(target, sgs)
                  case _ =>
                    log(s"No path for ${c.render}")
                    throw Exception("empty octree or octree bug?")
                }

              case _ =>
                log("empty octree or octree bug?")
                throw Exception("empty octree or octree bug?")
            }
        }).serialize
      )
    } else {
      postMessage("Not ready yet!")
    }
  }

  log("Initialized!")

}