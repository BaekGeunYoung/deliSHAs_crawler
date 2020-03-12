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

//                    println(it)
//                    println()

                    when {
                        isGeneralCase(restaurant[0]) -> {
                            val breakfast = convertGeneralCase(infos[1])
                            val lunch = convertGeneralCase(infos[2])
                            val dinner = convertGeneralCase(infos[3])
                        }
//                        restaurant[0] == "두레미담" -> {
//                            val breakfast = convertDuremidam(infos[1])
//                            val lunch = convertDuremidam(infos[2])
//                            val dinner = convertDuremidam(infos[3])
//                        }
                        restaurant[0] == "4식당" -> {
                            val firstBreakfast = convert4Sicdang(1, infos[1])
                            val firstLunch = convert4Sicdang(1, infos[2])
                            val firstDinner = convert4Sicdang(1, infos[3])

                            println(firstBreakfast)
                            println(firstLunch)
                            println(firstDinner)

                            println("====")

                            val secondBreakfast = convert4Sicdang(2,infos[1])
                            val secondLunch = convert4Sicdang(2, infos[2])
                            val secondDinner = convert4Sicdang(2, infos[3])

                            println(secondBreakfast)
                            println(secondLunch)
                            println(secondDinner)
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
//                        restaurant[0] == "220동식당" -> {
//                            val breakfast = convert220(infos[1])
//                            val lunch = convert220(infos[2])
//                            val dinner = convert220(infos[3])
//                        }
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