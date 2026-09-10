package com.example.focusscroll

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class FocusScrollAccessibilityService : AccessibilityService() {

    private var lastBlockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Only inspect YouTube and Instagram
        if (
            packageName != "com.google.android.youtube" &&
            packageName != "com.instagram.android"
        ) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        // INSTAGRAM DEBUGGING
        if (packageName == "com.instagram.android") {

            val isReel = containsInstagramReel(rootNode)

            if (isReel) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastBlockTime > 1500) {
                    lastBlockTime = currentTime

                    android.util.Log.d(
                        "FocusScroll",
                        "INSTAGRAM REEL DETECTED"
                    )

                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }

            return
        }

        // YOUTUBE SHORTS
        if (packageName == "com.google.android.youtube") {

            val isShort = containsRemixButton(rootNode)

            if (isShort) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastBlockTime > 1500) {
                    lastBlockTime = currentTime

                    android.util.Log.d(
                        "FocusScroll",
                        "SHORT DETECTED — REMIX BUTTON FOUND"
                    )

                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }
        }
    }
    private fun containsRemixButton(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        val description = node.contentDescription?.toString()

        if (
            node.className?.toString() == "android.widget.Button" &&
            description.equals("Remix", ignoreCase = true)
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)

                if (containsRemixButton(child)) {
                    child?.recycle()
                    return true
                }

                child?.recycle()
            } catch (e: Exception) {
                // Ignore inaccessible nodes
            }
        }

        return false
    }
    private fun containsInstagramReel(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        val description = node.contentDescription?.toString() ?: ""

        if (
            description.startsWith("Reel by ", ignoreCase = true) &&
            description.contains(
                "Double tap to play or pause",
                ignoreCase = true
            )
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)

                if (containsInstagramReel(child)) {
                    child?.recycle()
                    return true
                }

                child?.recycle()
            } catch (e: Exception) {
                // Ignore inaccessible nodes
            }
        }

        return false
    }

    private fun getAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        val textBuilder = StringBuilder()

        collectText(node, textBuilder)

        return textBuilder.toString()
    }
    private fun dumpNode(
        node: AccessibilityNodeInfo?,
        depth: Int = 0
    ) {
        if (node == null) return

        val indent = "  ".repeat(depth)

        android.util.Log.d(
            "FocusScroll",
            "$indent" +
                    "CLASS=${node.className} | " +
                    "ID=${node.viewIdResourceName} | " +
                    "TEXT=${node.text} | " +
                    "DESC=${node.contentDescription} | " +
                    "CLICKABLE=${node.isClickable} | " +
                    "SCROLLABLE=${node.isScrollable} | " +
                    "ENABLED=${node.isEnabled} | " +
                    "FOCUSABLE=${node.isFocusable} | "

        )

        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)

                if (child != null) {
                    dumpNode(child, depth + 1)
                    child.recycle()
                }
            } catch (e: Exception) {
                // Ignore inaccessible nodes
            }
        }
    }

    private fun collectText(
        node: AccessibilityNodeInfo?,
        textBuilder: StringBuilder
    ) {
        if (node == null) return

        node.text?.let { text ->
            textBuilder.append(text)
            textBuilder.append(" ")
        }

        node.contentDescription?.let { description ->
            textBuilder.append(description)
            textBuilder.append(" ")
        }

        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)

                if (child != null) {
                    collectText(child, textBuilder)
                    child.recycle()
                }
            } catch (e: Exception) {
                // Ignore inaccessible nodes
            }
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService
    }
}