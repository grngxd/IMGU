# IMGU
imgu is a small utility/wrapper inspired by [Wolfsurge's NVGU](https://github.com/Wolfsurge/NVGU/), which aims to make imgui easier to use in Java.
It is designed to be as simple as possible, and to be as close to the original imgui API as possible, while still providing some useful features (such as not having to write imgui::SameLine all the time).

## Usage
```kt
import cc.grng.imgu.IMGU;

fun main() {
    // ...

    val i = IMGU(handle)
        .createFont(
            "Roboto",
            GUI::class.java.getResourceAsStream("/Roboto.ttf")!!,
            hashMapOf(
                // ...
                "medium" to 16f,
                // ...
            )
        ).create()
    
    var counter = 0;
    
    while (!glfwWindowShouldClose(glfwWindowHandle)) {
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
    }
    
    i.destroy();
    // ...
}
```

pretty simple, right?

## Features
- Lambda-based API