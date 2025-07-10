# ⚡ DFS Visualizer - JavaFX

A modern, easy-to-use JavaFX desktop application to visualize **Depth First Search (DFS)** on a directed graph. Add colorful nodes, connect edges, and see DFS in action with a clean animated interface.

---

## 🚀 Features

- 🎨 Add nodes with custom colors  
- ➡️ Add directed edges with arrows  
- 🧭 Run DFS from any node and see the traversal live  
- ❌ Right-click to delete nodes or edges  
- 💾 Save graph as image (PNG)  
- 🔍 Zoom in/out with mouse scroll  
- 🔄 Reset entire graph  

---

## 🧰 Built With

- Java 11+
- JavaFX (controls, graphics, animation)
- IntelliJ IDEA or any Java IDE

---

## ▶️ How to Run

### 1. Download & Set Up JavaFX

- Download JavaFX SDK: [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
- Extract it to a known location (e.g., `C:/javafx-sdk`)

---

### 2. Compile the Project

```bash
javac --module-path "C:/javafx-sdk/lib" --add-modules javafx.controls -d out src/*.java
```

> 💡 Replace `"C:/javafx-sdk/lib"` with the path where you extracted the JavaFX SDK.

---

### 3. Run the Application

```bash
java --module-path "C:/javafx-sdk/lib" --add-modules javafx.controls -cp out Main
```

> ✅ Ensure your `Main.java` contains `public static void main(String[] args)` and extends `javafx.application.Application`.

---

## 📸 Screenshots

Here’s what the DFS Visualizer looks like in action:

![DFS Visualizer Screenshot](assets/screenshot.png)

---
