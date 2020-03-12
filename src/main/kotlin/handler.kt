import Parser.Companion.convert4Sicdang
import Parser.Companion.convertGeneralCase
import Parser.Companion.separateContact
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.jsoup.Jsoup

class Crawler: RequestHandler<Req, Unit> {
    override fun handleRequest(input: Req?, context: Context) {
        val logger = context.logger
    }

    fun crawl() {
        Jsoup.connect("http://snuco.snu.ac.kr/ko/foodmenu").get().run {
            select("table.views-table tbody")
                .select("tr")
                .forEach  lit@{
                    val infos = it.select("td")
                    if (infos.size != 4) return@lit

                    val restaurant = separateContact(infos[0].text())

                    if (restaurant[0] == "공대간이식당" || restaurant[0] == "소담마루" || restaurant[0] == "샤반" || restaurant[0] == "라운지오")
                        return@lit

                    println(it)
                    println()

                    when {
                        isGeneralCase(restaurant[0]) -> {
                            val regexForPrice = Regex("[0-9,]+원")
                            val breakfast = convertGeneralCase(infos[1], regexForPrice)
                            val lunch = convertGeneralCase(infos[2], regexForPrice)
                            val dinner = convertGeneralCase(infos[3], regexForPrice)
                        }
//                        restaurant[0] == "두레미담" -> {
//                            val breakfast = convertDuremidam(infos[1])
//                            val lunch = convertDuremidam(infos[2])
//                            val dinner = convertDuremidam(infos[3])
//                        }
                        restaurant[0] == "4식당" -> {
                            val regexForPrice = Regex("[0-9,]+원")

                            val firstRestaurantName = "4식당 1층"
                            // 4식당 1층 메뉴
                            val firstBreakfast = convert4Sicdang(1, infos[1], regexForPrice)
                            val firstLunch = convert4Sicdang(1, infos[2], regexForPrice)
                            val firstDinner = convert4Sicdang(1, infos[3], regexForPrice)

                            val secondRestaurantName = "4식당 2층"
                            // 4식당 2층 메뉴
                            val secondBreakfast = convert4Sicdang(2,infos[1], regexForPrice)
                            val secondLunch = convert4Sicdang(2, infos[2], regexForPrice)
                            val secondDinner = convert4Sicdang(2, infos[3], regexForPrice)

                        }
//                        restaurant[0] == "301동식당" -> {
//                            val firstBreakfast = convert301(1, infos[1])
//                            val firstLunch = convert301(1, infos[2])
//                            val firstDinner = convert301(1, infos[3])
//
//                            val secondBreakfast = convert301(2, infos[1])
//                            val secondLunch = convert301(2, infos[2])
//                            val secondDinner = convert301(2, infos[3])
//                        }
                        restaurant[0] == "220동식당" -> {
                            val regexForPrice = Regex("[0-9]\\.[0-9]")
                            val breakfast = convertGeneralCase(infos[1], regexForPrice)
                            val lunch = convertGeneralCase(infos[2], regexForPrice)
                            val dinner = convertGeneralCase(infos[3], regexForPrice)
                        }
                    }
            }
        }
    }

    private fun isGeneralCase(restaurantName: String?): Boolean {
        return restaurantName != null
                && restaurantName != "두레미담"
                && restaurantName != "4식당"
                && restaurantName != "301동식당"
                && restaurantName != "220동식당"
    }
}