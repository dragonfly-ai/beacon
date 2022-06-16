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

  var handle:SetTimeoutHandle = null

  val body = dom.window.document.body
  val memoizationProgress = dom.window.document.getElementById("memoizationProgress")
  val octreeProgress = dom.window.document.getElementById("octreeProgress")

  val height = 25

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
          if (s.equals("COMPLETED")) handle = nextColor(0)
          else println(s"Main received: $s")
        case _ => println(s"Main received: ${msg.data}")
      }
    }
  }

  worker.postMessage("Let's go!")

  def nextColor(wait:Int = 5000): SetTimeoutHandle = setTimeout(wait) {
    worker.postMessage(ARGB32.random().argb)
    handle = nextColor()
  }

  private def updateProgress(flag:String, p:Double):Unit = if (flag.equals("MEMOIZATION")){
    memoizationProgress.innerHTML = f"${100.0 * p}%.3f%%"
  } else {
    octreeProgress.innerHTML = f"${100.0 * p}%.3f%%"
  }

  private def colorDotScale(c:ARGB32):String = {
    s"display: inline-block; background-color: ${c.html()}; border: 1px solid var(--theme-border-color); border-radius: 50%; width: 1em; height: 1em; vertical-align: text-top;"
  }

  private def appendBeacon(sgs:ResultsMessage):Unit = {
    val tc:ARGB32 = sgs.target
    val result:ARGB32 = sgs.nearestMatch.approximateColor
    val sequence:List[ARGB32] = sgs.nearestMatch.sequence
    val reachable:Boolean = tc.argb == result.argb

    body.appendChild(
      table(
        tr(
          td(style := s"background-color: ${tc.html()}; border: 1px solid; width: 64px;")((0 until height).map(_ => br())),
          td(style := s"background-color: ${result.html()}; border: 1px solid; width: 64px;")(
            raw("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"),
            (0 until height - sequence.size).map(_ => br())
          )
        ),
        tr(
          td(
            if (reachable) "✅ " else "❌",
            br(),
            f"${100.0 * ARGB32.similarity(tc, result)}%.1f%%",
            br(),
            span(s"N = ${sequence.size}")
          ),
          td(
            sequence.map(
              c => div(
                span(style := colorDotScale(c))(br()),
                raw("&nbsp;"),
                img(src := s"./image/mcdye/${c.html().substring(1)}.png")
              )
            )
          ),
          td(raw("&nbsp;"))
        )
      ).render
    )
  }

}
