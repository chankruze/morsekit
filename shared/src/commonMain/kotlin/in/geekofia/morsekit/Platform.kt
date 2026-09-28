package `in`.geekofia.morsekit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform