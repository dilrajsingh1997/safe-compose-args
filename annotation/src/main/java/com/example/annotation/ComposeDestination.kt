package com.example.annotation

@Target(AnnotationTarget.CLASS)
annotation class ComposeDestination(val route: String)

//@Target(AnnotationTarget.CLASS)
//annotation class temp(val route: String, val args: Array<Pair<String, String>>)
