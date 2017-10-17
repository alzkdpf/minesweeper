package com.example.michaellim.minegame

import java.util.*
import org.jetbrains.anko.*

/**
 * Created by michaellim on 2017. 10. 16..
 */
/**
 * gen random digit
 */
fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

