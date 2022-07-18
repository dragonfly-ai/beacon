import Main.worker.postMessage
import beacon.StainedGlassSequence
import bridge.array.*
import org.scalajs.dom
import org.scalajs.dom.{MessageEvent, Worker, document}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}
import scalatags.JsDom.all.*
import ai.dragonfly.bitfrost.ColorContext.sRGB.{ARGB32, Lab}
import beacon.message.ResultsMessage

import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

object Main extends App {

  object DOMGlobals {
    @js.native
    @JSGlobal("showPicker")
    def showPicker(): Unit = js.native
  }

  var handle:SetTimeoutHandle = null

  val body = dom.window.document.body
  val memoizationProgress = dom.window.document.getElementById("memoizationProgress")
  val octreeProgress = dom.window.document.getElementById("octreeProgress")

  val beaconOutput = dom.window.document.getElementById("key")

  val height = 10

  var awakenCount:Int = 0

  object worker extends Worker("./js/worker.js") {
    this.onmessage = (msg:dom.MessageEvent) => {
      msg.data match {
        case arr:ARRAY[_] =>
          arr(0) match {
            case s:String if s.equals("STATUS") => updateProgress(arr(1).asInstanceOf[String], arr(2).asInstanceOf[Double])
            case _:Int => appendBeacon(ResultsMessage(arr.asInstanceOf[ARRAY[Int]]))
            case _ => println(s"Unknown Array Message Payload: ${arr(0)}")
          }
        case s:String =>
          if (s.equals("COMPLETED")) {
            DOMGlobals.showPicker()
            //document.getElementById("picker").removeAttribute("style")
            keepAwake()
          } else println(s"Main received: $s")
        case _ => println(s"Main received: ${msg.data}")
      }
    }
  }

  worker.postMessage("Let's go!")

  var qc:Int = ARGB32(255, 255, 255).argb

  def keepAwake(wait:Int = 1000): SetTimeoutHandle = setTimeout(wait) {
    worker.postMessage(qc)
    handle = keepAwake()
  }

  @JSExportTopLevel("searchColor")
  def searchColor(r:Int, g:Int, b:Int):Unit = {
    qc = ARGB32(r, g, b).argb
  }

  private def updateProgress(flag:String, p:Double):Unit = if (flag.equals("MEMOIZATION")){
    memoizationProgress.innerHTML = f"${100.0 * p}%.3f"
  } else {
    octreeProgress.innerHTML = f"${100.0 * p}%.3f"
  }

  private def colorDotScale(c:ARGB32):String = {
    s"display: inline-block; background-color: ${c.html()}; border: 1px solid var(--theme-border-color); border-radius: 50%; width: 1em; height: 1em; vertical-align: text-top;"
  }

  var lastTarget:Int = ARGB32(128, 128, 128).argb

  private def appendBeacon(sgs:ResultsMessage):Unit = {
    val tc:ARGB32 = sgs.target
    println(s"tc.argb == lastTarget ? ${tc.argb == lastTarget}")
    if (lastTarget != tc.argb) {
      lastTarget = tc.argb
      val result: ARGB32 = sgs.nearestMatch.approximateColor
      val sequence: List[ARGB32] = sgs.nearestMatch.sequence
      val reachable: Boolean = tc.argb == result.argb

      for (cn <- beaconOutput.childNodes) {
        beaconOutput.removeChild(cn)
      }

      beaconOutput.appendChild(
        table(style := "padding: 16px;")(
          tr(
            td(style := "padding: 8px;", colspan := "5")(
              if (reachable) {
                "Exact match: ✅"
              } else {
                "Match Similarity: " + f"${100.0 * ARGB32.similarity(tc, result)}%.1f%%"
              },
              br(),
              span(s"Minimum Number of Stained Glass Blocks: ${sequence.size}")
            )
          ),
          tr(
            td("Intended Color"),
            td(raw("&nbsp;")),
            td("Best Match"),
            td(raw("&nbsp;")),
            td("Stained Glass Sequence")
          ),
          tr(
            td(style := s"background-color: ${tc.html()}; border: 1px solid; width: 64px;")((0 until height).map(_ => br())),
            td(raw("&nbsp;")),
            td(style := s"background-color: ${result.html()}; border: 1px solid; width: 64px;")(
              raw("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"),
              (0 until height - sequence.size).map(_ => br())
            ),
            td(raw("&nbsp;")),
            td( sequence.map( c => div(span(style := colorDotScale(c))(raw("&nbsp;"), img(src := s"./image/mcdye/${c.html().substring(1)}.png")), br()) ) )
          ),
        ).render
      )
    }
  }

}
