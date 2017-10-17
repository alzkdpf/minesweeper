package com.example.michaellim.minegame

/**
 * Created by michaell on 2017. 10. 16..
 */
/**
 * box status
 */
enum class CHECK_STATUS(val value: Int) {
    CLOSE(0),
    OPEN(-1),
    FLAG(-4)
}

/**
 * mine status
 */
enum class MINE_STATUS(val value: Int) {
    NOT_SET(0),
    MINE(-3)
}

/**
 * left click or right click
 */
enum class SWITCH_BUTTON(val value: Int){
    OPEN(0),
    CHECK(1)
}