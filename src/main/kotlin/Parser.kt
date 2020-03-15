import SpecialCharacterHandler.Companion.decode
import org.jsoup.nodes.Element
import java.awt.Menu

class Parser {
    companion object {
        fun separateContact(str: String): Array<String?> {
            val regexForName = Regex(".*\\(")
            val regexForContact = Regex("\\(.*\\)")

            val name = regexForName.find(str)?.value
            val contact = regexForContact.find(str)?.value

            return arrayOf(name?.substring(0, name.length - 1), contact?.substring(1, contact.length - 1))
        }

        fun convertGeneralCase(html: Element, regexForPrice: Regex): List<MenuInfo> {
            return if (html.select("br").isEmpty()) {
                handleMessage(html.select("p").map { getMenuInfo(decode(it.text()), regexForPrice) }.toMutableList())
            }
            else {
                val longStr = html.select("p")[0].html()
                val validMenuStr = mutableListOf<String>()

                longStr.split("<br />")
                    .forEach {
                        if (!decode(it).trim().isBlank()) validMenuStr += it
                    }

                handleMessage(validMenuStr.map { getMenuInfo(decode(it), regexForPrice) }.toMutableList())
            }
        }

        fun convert4Sicdang(floor: Int, html: Element, regexForPrice: Regex): List<MenuInfo> {
            val menuInfos = html.select("p").map {
                    getMenuInfo(decode(it.text()), regexForPrice)
                }

            var firstFloorIdx: Int? = null
            var secondFloorIdx: Int? = null

            menuInfos.forEachIndexed { index, info ->
                if (info.msg == "1층") firstFloorIdx = index
                if (info.msg == "2층") secondFloorIdx = index
            }

            return if (firstFloorIdx != null && secondFloorIdx != null) {
                if (floor == 1) handleMessage(menuInfos.subList(1, secondFloorIdx!!))
                else handleMessage(menuInfos.subList(secondFloorIdx!! + 1, menuInfos.size))
            } else {
                handleMessage(menuInfos)
            }
        }

        fun convert301(case: Int, html: Element, regexForPrice: Regex): List<MenuInfo> {
            if(case == 1) {
                return if(html.text().contains("301푸드코트")) {
                    val longStr = html.select("p")[0].html()

                    val validMenuStr = mutableListOf<String>()

                    longStr.split("<br />")
                        .forEach {
                            if (!decode(it).isBlank()) validMenuStr += it
                        }

                    validMenuStr.map { getMenuInfo(decode(it), regexForPrice) }
                        .toMutableList()
                        .let { handleMessage(it.subList(1, it.size)) }
                } else {
                    listOf()
                }
            } else {
                if(html.text().contains("교직원식당")) {
                    val ret = mutableListOf<MenuInfo>()

                    val longStr1 = decode(html.select("p")[1].html())
                    val longStr2 = decode(html.select("p")[2].html())

                    val menuTypeOneName = "봄 - ${longStr1.split("<br />")[2]}"
                    val price = 6000

                    ret.add(MenuInfo(menuTypeOneName, price, null))

                    val validMenuStr = mutableListOf<String>()

                    longStr2.split("<br />")
                        .forEach {
                            if (!decode(it).isBlank()) validMenuStr += it
                        }

                    val typeTwoMenus = validMenuStr
                        .let { it.subList(1, it.size) }
                        .map { MenuInfo("소반 - $it", 5500, null) }

                    ret.addAll(typeTwoMenus)

                    return ret
                } else {
                    return listOf()
                }
            }
        }

        fun convertDuremidam(html: Element, regexForPrice: Regex): List<MenuInfo> {
            return if (html.text().contains("셀프(뷔페)코너")) {
                val menuName = "셀프 코너 - ${decode(html.select("p")[1].text())}"
                val price = 6000
                mutableListOf(MenuInfo(menuName, price, null))
            } else {
                handleMessage(html.select("p")
                    .map { getMenuInfo(decode(it.text()), regexForPrice) }
                    .toMutableList())
            }
        }

        private fun handleMessage(menuInfoList: List<MenuInfo>): List<MenuInfo> {
            val ret: List<MenuInfo> = mutableListOf<MenuInfo>().apply { addAll(menuInfoList) }

            if (ret.any { it.msg != null }) {
                var msg = ""
                ret.forEach {
                    if (it.msg != null) msg += " ${it.msg}"
                }

                ret.forEach {
                    it.msg = msg
                    it.isValid = false
                }
            }

            return ret
        }

        private fun getMenuInfo(str: String, regexForPrice: Regex): MenuInfo {
            val priceStr = regexForPrice.find(str)

            val price = priceStr?.value
                ?.replace(",", "")
                ?.replace("원", "")
                ?.let{
                    if(it.contains('.')) {
                        (it.toDouble() * 1000).toInt()
                    }
                    else it.toInt()
                }
            var menuName: String? = null
            var msg: String? = null

            priceStr?.let { mat ->
                menuName = str.substring(0, mat.range.first - 1)
            }

            if (price == null || menuName == null) {
                msg = str
            }

            return MenuInfo(
                name = menuName,
                price = price,
                msg = msg
            )
        }
    }
}