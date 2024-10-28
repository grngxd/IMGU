import cc.grng.imgu.IMGU
import cc.grng.imgu.IMGUStyle
import imgui.ImGui
import imgui.flag.ImGuiCol
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Font.createFont

class GUI {

    @Test
    fun windowTest() {
        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        val handle = GLFW.glfwCreateWindow(1280, 720, "Hello World!", 0, 0)
        GLFW.glfwMakeContextCurrent(handle)
        GL.createCapabilities()

        val i = IMGU(handle)
            .createImage(
                "image",
                GUI::class.java.getResourceAsStream("/image.jpg")!!
            )
            .createFont(
                "Roboto",
                GUI::class.java.getResourceAsStream("/Roboto.ttf")!!,
                hashMapOf(
                    "xsmall" to 8f,
                    "small" to 12f,
                    "medium" to 16f,
                    "large" to 24f,
                    "xlarge" to 32f
                )
            ).create()

        i.setStyle(
            IMGUStyle().apply {
                windowPadding = floatArrayOf(10f, 10f)
                windowRounding = 10f
                colors = hashMapOf(
                    // ImGuiCol.WindowBg to Color.decode("#FF0000"),
                )
            }
        )

        var counter = 0;

        while (!GLFW.glfwWindowShouldClose(handle)) {
            GLFW.glfwPollEvents()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            i.render() {
                i.pushFont("Roboto", "medium") {
                    i.window("IMGU ${IMGU.version}") {
                        ImGui.text("imgu also says hello. (${IMGU.version})")
                    }

                    i.window("Counter", Pair(60f, 150f)) {
                        i.inline(
                            { i.button("-") { counter-- } },
                            { ImGui.text("Count: $counter") },
                            { i.button("+") { counter++ } }
                        )
                    }

                    i.window("Image", Pair(60f, 300f)) {
                        val img = i.image("image", Pair(100f, 100f))
                        if (img != null) {
                            ImGui.text("Image size: ${img["width"]}x${img["height"]} (${img["renderedWidth"]}x${img["renderedHeight"]})")
                            ImGui.text("ID: ${img["id"]}")
                            ImGui.text("Aspect Ratio: ${img["aspectRatio"]}")
                        }
                        i.imageButton("image", Pair(100f, 20f)) {}
                    }

                    ImGui.showDemoWindow()
                }
            }

            GLFW.glfwSwapBuffers(handle)
        }

        // Clean up
        i.destroy()
        GLFW.glfwDestroyWindow(handle)
        GLFW.glfwTerminate()
    }
}