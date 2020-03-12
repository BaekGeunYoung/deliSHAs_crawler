class SpecialCharacterHandler {
    companion object {
        fun decode(str: String): String {
            var returnStr = str
            returnStr = returnStr.replace("<br>", "\n")
            returnStr = returnStr.replace("&gt;", ">")
            returnStr = returnStr.replace("&lt;", "<")
            returnStr = returnStr.replace("&quot;", "\"")
            returnStr = returnStr.replace("&nbsp;", " ")
            returnStr = returnStr.replace("&amp;", "&")

            return returnStr
        }
    }
}