package com.jsingh.XpenseMate

// 🚀 The Global State Machine
// Now, ANY file in your ui/sidebar or ui/components package can see these!

enum class AppScreen {
    LOGIN,
    PERMISSIONS,
    MAIN_APP
}

enum class SidebarScreen {
    BUDGET,
    DESIGN,
    CATEGORY,
    RECURRING
}