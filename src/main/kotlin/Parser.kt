import org.jsoup.nodes.Element
import java.awt.Menu

class Parser {
    companion object {
        fun convertGeneralCase(html: Element, regexForPrice: Regex): List<MenuInfo> {

            return if (html.select("br").isEmpty()) {
                html.select("p").map { getMenuInfo(it.text(), regexForPrice) }.toMutableList()
            }
            else {
                val longStr = html.select("p")[0].html()
                longStr.split("<br />").map { getMenuInfo(it, regexForPrice) }.toMutableList()
            }
        }

        fun getMenuInfo(str: String, regexForPrice: Regex): MenuInfo {
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

        fun separateContact(str: String): Array<String?> {
            val regexForName = Regex(".*\\(")
            val regexForContact = Regex("\\(.*\\)")

            val name = regexForName.find(str)?.value
            val contact = regexForContact.find(str)?.value

            return arrayOf(name?.substring(0, name.length - 1), contact?.substring(1, contact.length - 1))
        }

        fun convert4Sicdang(floor: Int, html: Element, regexForPrice: Regex): List<MenuInfo> {
            val menuInfos = html.select("p").map {
                    getMenuInfo(it.text(), regexForPrice)
                }

            var firstFloorIdx: Int? = null
            var secondFloorIdx: Int? = null

            menuInfos.forEachIndexed { index, info ->
                if (info.msg == "1층") firstFloorIdx = index
                if (info.msg == "2층") secondFloorIdx = index
            }

            return if (firstFloorIdx != null && secondFloorIdx != null) {
                if (floor == 1) menuInfos.subList(1, secondFloorIdx!!)
                else menuInfos.subList(secondFloorIdx!! + 1, menuInfos.size)
            } else {
                menuInfos
            }
        }

        fun convert301(case: Int, html: Element, regexForPrice: Regex): List<MenuInfo> {
            if(case == 1) {
                return if(html.text().contains("301푸드코트")) {
                    val longStr = html.select("p")[0].html()

                    longStr.split("<br />")
                        .map {
                            getMenuInfo(it, regexForPrice)
                        }
                        .toMutableList()
                        .let { it.subList(1, it.size) }
                } else {
                    listOf()
                }
            } else {
                if(html.text().contains("교직원식당")) {
                    val ret = mutableListOf<MenuInfo>()

                    val longStr1 = html.select("p")[1].html()
                    val longStr2 = html.select("p")[2].html()

                    val menuTypeOneName = "봄 - ${longStr1.split("<br />")[2]}"
                    val price = 6000

                    ret.add(MenuInfo(menuTypeOneName, price, null))

                    val typeTwoMenus = longStr2.split("<br />")
                        .let { it.subList(1, it.size) }
                        .map { MenuInfo("소반 - $it", 5500, null) }

                    ret.addAll(typeTwoMenus)

                    return ret
                } else {
                    return listOf()
                }
            }
        }
    }
}