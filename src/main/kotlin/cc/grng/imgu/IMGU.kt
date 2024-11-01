package cc.grng.imgu

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.internal.ImGuiContext
import imgui.type.*
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBImage.*
import java.awt.Color
import java.io.InputStream
import java.nio.ByteBuffer

open class IMGU(val handle: Long = -1L) {

    companion object {
        val version = "0.1"
    }

    private val imGuiGlfw = ImGuiImplGlfw()
    private val imGuiGl3 = ImGuiImplGl3()

    private val fonts: HashMap<String, Map<InputStream, HashMap<String, Float>>> = hashMapOf()
    private val imFonts: HashMap<String, Map<String, ImFont>> = hashMapOf()

    private var created = false

    lateinit var ctx: ImGuiContext

    // Initialization
    open fun create(): IMGU {
        if (created) {
            throw IllegalStateException("[IMGU] IMGU has already been initialized.")
        }

        created = true
        if (handle == -1L) {
            throw IllegalStateException("[IMGU] Display Handle provided is invalid. Has the display been created?")
        }

        ctx = ImGui.createContext()
        ImGui.setCurrentContext(ctx)

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
            throw IllegalStateException("[IMGU] Failed to initialize ImGui")
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

    // Images
    val images: HashMap<String, Map<String, Any>> = hashMapOf()

    fun createImageFromMemory(name: String, buffer: ByteBuffer): Map<String, Any> {
        // Initialize width and height arrays
        val widthArr = IntArray(1)
        val heightArr = IntArray(1)
        val compArr = IntArray(1)

        // Load image from memory
        val image = stbi_load_from_memory(buffer, widthArr, heightArr, compArr, 4)
        if (image == null) {
            throw IllegalStateException("Failed to load image: ${stbi_failure_reason()}")
        }

        val width = widthArr[0]
        val height = heightArr[0]

        // Generate texture ID
        val id = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, id)

        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // Upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)

        // Free the image memory
        stbi_image_free(image)

        return mapOf(
            "id" to id,
            "width" to width,
            "height" to height,
            "aspectRatio" to (width.toFloat() / height.toFloat())
        )
    }

    fun createImage(name: String, stream: InputStream): IMGU {
        val bytes = stream.readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
            put(bytes)
            flip() // Ensure the buffer is ready for reading
        }

        val i = createImageFromMemory(name, buffer)
        images[name] = i

        return this
    }

    fun getImage(name: String): Map<String, Any> {
        return images[name]!!
    }

    // Font Management
    fun createFont(name: String, font: InputStream, sizes: HashMap<String, Float>): IMGU {
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

    fun setStyle(imguStyle: IMGUStyle): IMGU {
        val style = ImGui.getStyle()

        imguStyle.javaClass.declaredFields.forEach { field ->
            field.isAccessible = true
            val methodName = "set${field.name[0].toUpperCase()}${field.name.substring(1)}"
            val method = style.javaClass.methods.find { it.name == methodName }

            if (method != null) {
                when (field.type) {
                    Float::class.java -> method.invoke(style, field.getFloat(imguStyle))
                    FloatArray::class.java -> {
                        val floatArray = field.get(imguStyle) as FloatArray
                        when (floatArray.size) {
                            1 -> method.invoke(style, floatArray[0])
                            2 -> method.invoke(style, floatArray[0], floatArray[1])
                            3 -> method.invoke(style, floatArray[0], floatArray[1], floatArray[2])
                            4 -> method.invoke(style, floatArray[0], floatArray[1], floatArray[2], floatArray[3])
                            else -> throw IllegalArgumentException("Unsupported FloatArray size: ${floatArray.size}")
                        }
                    }
                    else -> {
                        when (field.name) {
                            "colors" -> {
                                val colors = field.get(imguStyle) as HashMap<Int, Color>
                                colors.forEach { (key, value) ->
                                    style.setColor(key, value.red, value.green, value.blue, value.alpha)
                                }
                            }
                            else -> throw IllegalArgumentException("Unsupported field type: ${field.type}")
                        }
                    }
                }
            } else {
                println("Method $methodName not found for field ${field.name}")
            }

            println("Setting ${field.name} to ${field.get(imguStyle)}")
        }

        return this
    }

    // Rendering
    open fun render(r: Runnable) {
        ImGui.setCurrentContext(ctx)
        newFrame()

        r.run()

        render()
    }

    open fun frame(r: Runnable) {
        ImGui.setCurrentContext(ctx)
        newFrame()

        r.run()

        ImGui.endFrame()
    }

    fun newFrame() {
        val width = IntArray(1)
        val height = IntArray(1)
        GLFW.glfwGetFramebufferSize(handle, width, height)
        ImGui.getIO().setDisplaySize(width[0].toFloat(), height[0].toFloat())

        imGuiGlfw.newFrame()
        ImGui.newFrame()
    }

    fun render() {
        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
    }

    // Utility Methods
    fun window(name: String, flags: Int, pos: Pair<Float, Float>, size: Pair<Float, Float>, r: Runnable) {
        window(name, flags, pos, ImGuiCond.Once, size, r)
    }

    fun window(name: String, flags: Int, pos: Pair<Float, Float>, cond: Int, size: Pair<Float, Float>,  r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second, cond)
        ImGui.setNextWindowSize(size.first, size.second, cond)
        window(name, flags, r)
    }

    fun window(name: String, flags: Int, pos: Pair<Float, Float>, pivot: Pair<Float, Float>, cond: Int, size: Pair<Float, Float>, r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second, cond, pivot.first, pivot.second)
        ImGui.setNextWindowSize(size.first, size.second, cond)
        window(name, flags, r)
    }

    fun window(name: String, pos: Pair<Float, Float>, size: Pair<Float, Float>, r: Runnable) {
        window(name, 0, pos, size, r)
    }

    fun window(name: String, flags: Int, pos: Pair<Float, Float>, r: Runnable) {
        window(name, flags, pos, r)
    }

    fun window(name: String, flags: Int, pos: Pair<Float, Float>, cond: Int, r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second, cond)
        window(name, flags, r)
    }
    fun window(name: String, flags: Int, pos: Pair<Float, Float>, pivot: Pair<Float, Float>, cond: Int, r: Runnable) {
        ImGui.setNextWindowPos(pos.first, pos.second, cond, pivot.first, pivot.second)
        window(name, flags, r)
    }

    // you can pass a pair by doing Pair(1f, 1f)
    fun window(name: String, pos: Pair<Float, Float>, r: Runnable) {
        window(name, 0, pos, ImGuiCond.Once, r)
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

    fun button(label: String, r: Runnable) {
        if (ImGui.button(label)) {
            r.run()
        }
    }

    fun text(text: String, color: Int = ImGui.getColorU32(ImGuiCol.Text), wrapped: Boolean = false) {
        ImGui.pushStyleColor(ImGuiCol.Text, color)
        if (wrapped) {
            ImGui.textWrapped(text)
        } else {
            ImGui.text(text)
        }
        ImGui.popStyleColor()
    }

    fun input(label: String, value: ImString) {
        ImGui.inputText(label, value)
    }

    fun input(label: String, value: ImInt) {
        ImGui.inputInt(label, value)
    }

    fun input(label: String, value: ImFloat) {
        ImGui.inputFloat(label, value)
    }

    fun slider(label: String, value: IntArray, min: Int, max: Int, format: String) {
        ImGui.sliderInt(label, value, min, max)
    }

    fun slider(label: String, value: FloatArray, min: Float, max: Float, format: String) {
        ImGui.sliderFloat(label, value, min, max)
    }

    fun slider(label: String, value: IntArray, min: Int, max: Int) {
        ImGui.sliderInt(label, value, min, max)
    }

    fun slider(label: String, value: FloatArray, min: Float, max: Float) {
        ImGui.sliderFloat(label, value, min, max)
    }


    fun checkbox(label: String, value: ImBoolean, r: Runnable) {
        if (ImGui.checkbox(label, value)) {
            r.run()
        }
    }

    fun separator() {
        ImGui.separator()
    }

    fun separator(vararg runnables: Runnable) {
        runnables.forEachIndexed { index, runnable ->
            runnable.run()
            if (index < runnables.size) {
                ImGui.separator()
            }
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

    fun sameLine(vararg runnables: Runnable) {
        inline(*runnables)
    }

    fun image(name: String, wh: Pair<Float, Float>): Map<String, Any> {
        val image = images[name] ?: throw IllegalArgumentException("Image $name not found")
        val id = image["id"] ?: throw IllegalArgumentException("Image $name does not have an id")
        val imgWidth = image["width"] ?: throw IllegalArgumentException("Image $name does not have a width")
        val imgHeight = image["height"] ?: throw IllegalArgumentException("Image $name does not have a height")

        ImGui.image(id as Int, wh.first, wh.second)

        return mapOf(
            "id" to id,
            "width" to imgWidth,
            "height" to imgHeight,
            "renderedWidth" to wh.first,
            "renderedHeight" to wh.second,
            "aspectRatio" to (imgWidth as Int / imgHeight as Int).toFloat()
        )
    }


    fun imageButton(name: String, wh: Pair<Float, Float>, r: Runnable): Map<String, Any> {
        val image = images[name] ?: throw IllegalArgumentException("Image $name not found")
        val id = image["id"] ?: throw IllegalArgumentException("Image $name does not have an id")
        val imgWidth = image["width"] ?: throw IllegalArgumentException("Image $name does not have a width")
        val imgHeight = image["height"] ?: throw IllegalArgumentException("Image $name does not have a height")

        if (ImGui.imageButton(id as Int, wh.first, wh.second)) {
            r.run()
        }

        return mapOf(
            "id" to id,
            "width" to imgWidth,
            "height" to imgHeight,
            "renderedWidth" to wh.first,
            "renderedHeight" to wh.second,
            "aspectRatio" to (imgWidth as Int / imgHeight as Int).toFloat()
        )
    }
}