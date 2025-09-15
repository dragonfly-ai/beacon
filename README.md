# beacon

If you want to design minecraft beacon colors right now, you can find a current version of the app here: https://beacon.colortree.net/ alternatively, you may clone this repository and build it from source with `sbt fullOptJS`.

<img src="https://raw.githubusercontent.com/dragonfly-ai/beacon/main/docs/image/screenshot01.png" alt="Screenshot of working Beacon app.">

_____________________________________________________

In Minecraft, beacons project vertical columns of light directly into the sky and imbue players with enhanced abilities.
Players can change the color of a beacon's light beam by channeling it through a sequence of stained glass blocks.
This affords a much greater diversity of possible beacon colors than dye colors in minecraft, yet far fewer than the number of colors available in sRGB.

The final color isn't a mere average of the colors in the sequence.  Rather, the top color always contributes 50% and each color below contributes half as much as the color above it.
Because the relevance of each color below the top follows a pattern of exponential falloff, players may find it difficult to predict which stained glass sequences will produce a specific color.

The Beacon app solves this problem by computing every possible beacon color with a technique called Memoization or Dynamic Programming, converting them to the L\*u\*v\* perceptually uniform color space, 
then making them searchable by storing them in a spacial datastructure called an Octree.

The following table shows the percentage contributions of each color in a stained glass sequence; the max sequence length is 21 because any colors you can get with sequences longer than 21 blocks can be obtained with a shorter sequence.

<table style="font-size:10px;">
<tr><td>Stained Glass Levels</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td><td>15</td><td>16</td><td>17</td><td>18</td><td>19</td><td>20</td><td>21</td></tr>
<tr><td>Color 1</td><td>100.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td><td>50.0%</td></tr>
<tr><td>Color 2</td><td> </td><td>50.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td><td>25.0%</td></tr>
<tr><td>Color 3</td><td> </td><td> </td><td>25.0%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td><td>12.5%</td></tr>
<tr><td>Color 4</td><td> </td><td> </td><td> </td><td>12.5%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td><td>6.25%</td></tr>
<tr><td>Color 5</td><td> </td><td> </td><td> </td><td> </td><td>6.25%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td><td>3.125%</td></tr>
<tr><td>Color 6</td><td> </td><td> </td><td> </td><td> </td><td> </td><td>3.125%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td><td>1.5625%</td></tr>
<tr><td>Color 7</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>1.5625%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td><td>0.7813%</td></tr>
<tr><td>Color 8</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.7813%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td><td>0.3906%</td></tr>
<tr><td>Color 9</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.3906%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td><td>0.1953%</td></tr>
<tr><td>Color 10</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.1953%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td><td>0.0977%</td></tr>
<tr><td>Color 11</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0977%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td><td>0.0488%</td></tr>
<tr><td>Color 12</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0488%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td><td>0.0244%</td></tr>
<tr><td>Color 13</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0244%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td><td>0.0122%</td></tr>
<tr><td>Color 14</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0122%</td><td>0.0061%</td><td>0.0061%</td><td>0.0061%</td><td>0.0061%</td><td>0.0061%</td><td>0.0061%</td><td>0.0061%</td></tr>
<tr><td>Color 15</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0061%</td><td>0.0031%</td><td>0.0031%</td><td>0.0031%</td><td>0.0031%</td><td>0.0031%</td><td>0.0031%</td></tr>
<tr><td>Color 16</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0031%</td><td>0.0015%</td><td>0.0015%</td><td>0.0015%</td><td>0.0015%</td><td>0.0015%</td></tr>
<tr><td>Color 17</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0015%</td><td>0.0008%</td><td>0.0008%</td><td>0.0008%</td><td>0.0008%</td></tr>
<tr><td>Color 18</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0008%</td><td>0.0004%</td><td>0.0004%</td><td>0.0004%</td></tr>
<tr><td>Color 19</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0004%</td><td>0.0002%</td><td>0.0002%</td></tr>
<tr><td>Color 20</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0002%</td><td>0.0001%</td></tr>
<tr><td>Color 21</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>0.0001%</td></tr>
</table>

As you can see, the farther down the column, the more negligible the percentage, so in sequences deeper than 21 blocks, the lowest colors have no meaningful effect on the colors.

# Acknowledgements:

This project, written in [Scala.js](https://www.scala-js.org/), makes good use of the Scala color science library [Uriel](https://github.com/dragonfly-ai/uriel), the Octree from [Spatial](https://github.com/dragonfly-ai/spatial), vector math from [S.L.A.S.H.](https://github.com/dragonfly-ai/slash), and native arrays from [NArr](https://github.com/dragonfly-ai/narr).

It was envisioned by [s5bug](https://github.com/s5bug), features a font by [Heaven Castrato](https://www.fontspace.com/roboto-remix-font-f26577) and a color picker by [Ivan Matveev](https://github.com/ivanvmat/color-picker).
