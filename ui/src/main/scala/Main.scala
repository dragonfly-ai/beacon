import Main.worker.postMessage
import beacon.StainedGlassSequence
import narr.*
import org.scalajs.dom
import org.scalajs.dom.{MessageEvent, Worker, document}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}
import scalatags.JsDom.all.*
import scalatags.JsDom.all.span as SPAN
import ai.dragonfly.uriel.ColorContext.sRGB.{ARGB32, Luv}
import beacon.message.ResultsMessage

import scala.collection.mutable
import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

/*
Show the color!  Hex and RGB.
 */

object Main extends App {

  object DOMGlobals {
    @js.native
    @JSGlobal("showPicker")
    def showPicker(): Unit = js.native
  }

  var keepAliveHandle:SetTimeoutHandle = null
  var colorQueryHandle:SetTimeoutHandle = null

  val body = dom.window.document.body
  val memoizationProgress = dom.window.document.getElementById("memoizationProgress")
  val octreeProgress = dom.window.document.getElementById("octreeProgress")

  val beaconOutput = dom.window.document.getElementById("key")
  val sample = dom.window.document.getElementById("sample")

  val height = 10

  var awakenCount:Int = 0

  object worker extends Worker("./js/worker.js") {
    this.onmessage = (msg:dom.MessageEvent) => {
      msg.data match {
        case s: String =>
          if (s.equals("COMPLETED")) {
            DOMGlobals.showPicker()
            //document.getElementById("picker").removeAttribute("style")
            keepAwake()
          } else println(s"Main received: $s")
        case arr:IntArray => appendBeacon(ResultsMessage(arr.asInstanceOf[NArray[ARGB32]]))
        case arr:js.Array[Any] =>
          arr(0) match {
            case s:String if s.equals("STATUS") =>
              println(s"${arr(0)}, ${arr(1)}, ${arr(2)}")
              updateProgress(arr(1).asInstanceOf[String], arr(2).asInstanceOf[Float])
            case _ => println(s"Unknown Array Message Payload: ${arr(0)}")
          }
        case _ => println(s"Main received: ${msg.data}")
      }
    }
  }

  worker.postMessage("Let's go!")

  var last:Long = System.currentTimeMillis()

  var qc:Int = 0
  var lqc:Int = 0

  def keepAwake(wait:Int = 3000): SetTimeoutHandle = setTimeout(wait) {
    worker.postMessage("AWAKEN")
    keepAliveHandle = keepAwake()
  }

  @JSExportTopLevel("executeQuery")
  def executeQuery():Unit = {
    val now:Long = System.currentTimeMillis()
    if (now - last > 100) {
      colorQueryHandle = setTimeout(100) { worker.postMessage(qc) }
    }
  }

  @JSExportTopLevel("searchColor")
  def searchColor(r:Int, g:Int, b:Int):Unit = {
    val temp:ARGB32 = ARGB32(r, g, b)
    qc = temp.asInstanceOf[Int]
    sample.setAttribute("style", s"width: 180px; height: 120px; border-radius: 8px; background-color: ${temp.html()};")
  }

  val zeroProgress:ARGB32 = ARGB32(0xFFB02E26)
  val finishedProgress:ARGB32 = ARGB32(0xFF80C71F)
  private def updateProgress(flag:String, p:Float):Unit = if (flag.equals("MEMOIZATION")){
    memoizationProgress.innerHTML = f"${100.0 * p}%.2f"
    memoizationProgress.setAttribute("style", s"color: ${ARGB32.weightedAverage(zeroProgress, 1f - p, finishedProgress, p).html()}")
  } else {
    octreeProgress.innerHTML = f"${100.0 * p}%.2f"
    octreeProgress.setAttribute("style", s"color: ${ARGB32.weightedAverage(zeroProgress, 1f - p, finishedProgress, p).html()}")
  }

  private def colorDotScale(c:ARGB32):String = {
    s"display: inline-block; background-color: ${c.html()}; border: 1px solid var(--theme-border-color); border-radius: 50%; width: 1em; height: 1em; vertical-align: text-top;"
  }

  var lastTarget:ARGB32 = ARGB32(128, 128, 128)

  private def appendBeacon(sgs:ResultsMessage):Unit = {
    val tc:ARGB32 = sgs.target
    if (lastTarget.asInstanceOf[Int] != tc.asInstanceOf[Int]) {
      lastTarget = tc
      val result: ARGB32 = sgs.nearestMatch.approximateColor
      val sequence: NArray[ARGB32] = sgs.nearestMatch.sequence.asInstanceOf[NArray[ARGB32]]
      val reachable: Boolean = tc == result

      for (cn <- beaconOutput.childNodes) {
        beaconOutput.removeChild(cn)
      }

      beaconOutput.appendChild(
        table(
          tr(
            td(style := "padding: 8px;", colspan := "5")(
              SPAN(style := "font-size: 28px;")("Results:"),
              br(),
              if (reachable) {
                s"✅ Exact Match"
              } else {
                SPAN("❌ Exact Match", br(), s"Similarity: " + f"${100.0 * ARGB32.similarity(tc, result)}%.1f%%")
              },
              br(),
              "RGB: ", SPAN(style := "color:#FF0000")(result.red), ", ", SPAN(style := "color:#00FF00")(result.green), ", ", SPAN(style := "color:#0000FF")(result.blue), br(), s"HTML: ${result.html()}"
            )
          ),
          tr(
            td("Closest"),
            td(raw("&nbsp;")),
            td(s"${sequence.size} Block Sequence")
          ),
          tr(
            td(style := s"background-color: ${result.html()}; border: 1px solid; width: 64px;")(
              raw("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"),
              (0 until Math.max(10, height - sequence.size)).map(_ => br())
            ),
            td(raw("&nbsp;")),
            td(style := "margin: auto; text-align: center; border-style: solid; border-width: 1px;")(
              (Seq[ARGB32]().appendedAll(sequence.iterator)).map(
                c => div(
                  SPAN(style := colorDotScale(c))(raw("&nbsp;"), img(src := s"./image/mcdye/${c.html().substring(1)}.png")),
                  br()
                )
              )
            )
          ),
        ).render
      )
    }
  }

}
