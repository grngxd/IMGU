import cc.grng.imgu.IMGU
import imgui.ImGui
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class GUI {
    lateinit var i: IMGU

    /*
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
        !!!!! Don't forget to run tests with the -Pdev property flag !!!!!
     */

    @Test
    fun windowTest() {
        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        val handle = GLFW.glfwCreateWindow(1280, 720, "Hello World!", 0, 0)
        GLFW.glfwMakeContextCurrent(handle)
        GL.createCapabilities()

        i = IMGU(handle)
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