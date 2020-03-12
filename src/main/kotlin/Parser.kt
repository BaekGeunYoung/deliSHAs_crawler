import org.jsoup.nodes.Element
import java.awt.Menu

class Parser {
    companion object {
        fun convertGeneralCase(html: Element): List<MenuInfo> {

            return if (html.select("br").isEmpty()) {
                html.select("p").map { getMenuInfo(it.text()) }.toMutableList()
            }
            else {
                val longStr = html.select("p")[0].text()

                longStr.split("<br />").map { getMenuInfo(it) }.toMutableList()
            }
        }

        fun getMenuInfo(str: String): MenuInfo {
            val regexForPrice = Regex("[0-9,]+원")
            val priceStr = regexForPrice.find(str)

            val price = priceStr?.value?.replace(",", "")?.replace("원", "")?.toInt()
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

        fun convert4Sicdang(floor: Int, html: Element): List<MenuInfo> {
            val menuInfos = html.select("p").map {
                    getMenuInfo(it.text())
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
    }
}