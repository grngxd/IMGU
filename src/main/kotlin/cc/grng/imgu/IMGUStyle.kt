package cc.grng.imgu

import imgui.flag.ImGuiCol
import lombok.Builder
import java.awt.Color

@Builder
open class IMGUStyle {
    var windowPadding: FloatArray = floatArrayOf(8f, 8f)
    var windowRounding: Float = 7f
    var colors: HashMap<Int, Color> = HashMap()
}