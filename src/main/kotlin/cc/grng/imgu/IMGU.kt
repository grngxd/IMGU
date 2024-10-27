package cc.grng.imgu

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.lwjgl.glfw.GLFW
import java.io.InputStream

class IMGU(val handle: Long = -1L) {

    companion object {
        val version = "0.1"
    }

    private val imGuiGlfw = ImGuiImplGlfw()
    private val imGuiGl3 = ImGuiImplGl3()

    private val fonts: HashMap<String, Map<InputStream, HashMap<String, Float>>> = hashMapOf()
    private val imFonts: HashMap<String, Map<String, ImFont>> = hashMapOf()

    private var created = false

    // Initialization
    open fun create(): IMGU {
        if (created) {
            throw IllegalStateException("IMGU has already been initialized.")
        }

        created = true
        if (handle == -1L) {
            throw IllegalStateException("Display Handle provided is invalid. Has the display been created?")
        }

        ImGui.createContext()
        val io = ImGui.getIO()
        io.iniFilename = null
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)

        val fontConfig = ImFontConfig()
        val fontAtlas = io.fonts
        fontAtlas.addFontDefault()

        fonts.forEach { (name, font) ->
            val fontMap = mutableMapOf<String, ImFont>()
            font.forEach { (stream, sizes) ->
                val fontData = stream.readBytes()
                sizes.forEach { (sizeName, size) ->
                    val imFont = fontAtlas.addFontFromMemoryTTF(fontData, size, fontConfig)
                    fontMap[sizeName] = imFont
                    println("Loaded font: $name, size: $sizeName")
                }
            }
            imFonts[name] = fontMap
        }

        fontConfig.destroy()
        fontAtlas.build()

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val style = ImGui.getStyle()
            style.windowRounding = 0.0f
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1f))
        }

        println("Initializing ImGui GL3")
        imGuiGl3.init()
        val initialized = imGuiGlfw.init(handle, true)
        if (!initialized) {
            throw IllegalStateException("Failed to initialize ImGui")
        }

        return this
    }

    open fun destroy() {
        ImGui.getIO().fonts.clear()
        imFonts.clear()
        imGuiGlfw.dispose()
        imGuiGl3.dispose()
        ImGui.destroyContext()
    }

    // Font Management
    open fun createFont(name: String, font: InputStream, sizes: HashMap<String, Float>): IMGU {
        if (created) {
            throw IllegalStateException("[IMGU] Cannot create font after IMGU has been initialized")
        }

        fonts[name] = mapOf(font to sizes)
        return this
    }

    fun getFont(name: String, size: String): ImFont? {
        return imFonts[name]?.get(size)
    }

    fun pushFont(name: String, size: String) {
        ImGui.pushFont(getFont(name, size))
    }

    fun pushFont(name: String, size: String, r: Runnable) {
        pushFont(name, size)
        r.run()
        popFont()
    }

    fun popFont() {
        ImGui.popFont()
    }

    // Rendering
    fun render(r: Runnable) {
        val width = IntArray(1)
        val height = IntArray(1)
        GLFW.glfwGetFramebufferSize(handle, width, height)
        ImGui.getIO().setDisplaySize(width[0].toFloat(), height[0].toFloat())

        imGuiGlfw.newFrame()
        ImGui.newFrame()

        r.run()

        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
    }

    // Utility Methods
    fun button(label: String, r: Runnable) {
        if (ImGui.button(label)) {
            r.run()
        }
    }

    fun inline(vararg runnables: Runnable) {
        runnables.forEachIndexed { index, runnable ->
            runnable.run()
            if (index < runnables.size) {
                ImGui.sameLine()
            }
        }
    }

    fun window(name: String, flags: Int, pos: Pair<Float, Float>, size: Pair<Float, Float>, r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second)
        ImGui.setNextWindowSize(size.first, size.second)
        window(name, flags, r)
    }

    fun window(name: String, pos: Pair<Float, Float>, size: Pair<Float, Float>, r: Runnable) {
        window(name, 0, pos, size, r)
    }

    // you can pass a pair by doing Pair(1f, 1f)
    fun window(name: String, pos: Pair<Float, Float>, r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second)
        ImGui.begin(name)
        r.run()
        ImGui.end()
    }

    fun window(name: String, flags: Int, r: Runnable) {
        ImGui.begin(name, flags)
        r.run()
        ImGui.end()
    }

    fun window(name: String, r: Runnable) {
        window(name, 0, r)
    }

    fun window(r: Runnable) {
        window("Window", r)
    }
}