import Parser.Companion.convert301
import Parser.Companion.convert4Sicdang
import Parser.Companion.convertDuremidam
import Parser.Companion.convertGeneralCase
import Parser.Companion.separateContact
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Crawler: RequestHandler<Any, Unit> {
    private val dataSource = DataSource()
    private val connection = dataSource.getConnection()

    override fun handleRequest(input: Any?, context: Context) {
        val logger = context.logger
        val formatter = DateTimeFormatter.ofPattern("MM/dd/YYYY")
        val today = LocalDate.now()

        val baseUrl = Constants.BASE_CRAWL_URL

        // 이전에 저장돼있던 메뉴들을 모두 삭제한다.
        deleteAll()

        for (i in (0 until Constants.CRAWL_DAYS_UNTIL)) {
            val date = today.plusDays(i.toLong())
            val day = formatter.format(date)
            val url = "$baseUrl?field_menu_date_value_1[value][date]=&field_menu_date_value[value][date]=$day"

            println("crawling start : $day")
            crawl(url, date)
            println("==========================================================================")
        }
    }

    fun crawl(url: String, date: LocalDate) {
        val ret = mutableListOf<Menu>()

        Jsoup.connect(url).get().run {
            select("table.views-table tbody")
                .select("tr")
                .forEach  lit@{
                    val infos = it.select("td")
                    if (infos.size != 4) return@lit

                    val restaurant = separateContact(infos[0].text())

                    if (restaurant[0] == "공대간이식당" || restaurant[0] == "소담마루" || restaurant[0] == "샤반" || restaurant[0] == "라운지오")
                        return@lit

                    when {
                        isGeneralCase(restaurant[0]) -> {
                            val regexForPrice = Regex("[0-9,]+원")

                            val breakfast = convertGeneralCase(infos[1], regexForPrice)
                            val lunch = convertGeneralCase(infos[2], regexForPrice)
                            val dinner = convertGeneralCase(infos[3], regexForPrice)

                            ret.addAll(breakfast.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(lunch.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.LUNCH, date) })
                            ret.addAll(dinner.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.DINNER, date) })

                        }
                        restaurant[0] == "두레미담" -> {
                            val regexForPrice = Regex("[0-9,]+원")

                            val breakfast = convertDuremidam(infos[1], regexForPrice)
                            val lunch = convertDuremidam(infos[2], regexForPrice)
                            val dinner = convertDuremidam(infos[3], regexForPrice)

                            ret.addAll(breakfast.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(lunch.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.LUNCH, date) })
                            ret.addAll(dinner.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.DINNER, date) })
                        }
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

                            ret.addAll(firstBreakfast.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(firstLunch.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.LUNCH, date) })
                            ret.addAll(firstDinner.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.DINNER, date) })
                            ret.addAll(secondBreakfast.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(secondLunch.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.LUNCH, date) })
                            ret.addAll(secondDinner.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.DINNER, date) })
                        }
                        restaurant[0] == "301동식당" -> {
                            val regexForPrice = Regex("[0-9,]+원")

                            val firstRestaurantName = "301동 푸드코트"

                            val firstBreakfast = convert301(1, infos[1], regexForPrice)
                            val firstLunch = convert301(1, infos[2], regexForPrice)
                            val firstDinner = convert301(1, infos[3], regexForPrice)

                            val secondRestaurantName = "301동 교직원식당"

                            val secondBreakfast = convert301(2, infos[1], regexForPrice)
                            val secondLunch = convert301(2, infos[2], regexForPrice)
                            val secondDinner = convert301(2, infos[3], regexForPrice)

                            ret.addAll(firstBreakfast.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(firstLunch.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.LUNCH, date) })
                            ret.addAll(firstDinner.map { menuInfo -> Menu.of(firstRestaurantName, menuInfo, Time.DINNER, date) })
                            ret.addAll(secondBreakfast.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(secondLunch.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.LUNCH, date) })
                            ret.addAll(secondDinner.map { menuInfo -> Menu.of(secondRestaurantName, menuInfo, Time.DINNER, date) })
                        }
                        restaurant[0] == "220동식당" -> {
                            val regexForPrice = Regex("[0-9]\\.[0-9]|[0-9,]+원")

                            val breakfast = convertGeneralCase(infos[1], regexForPrice)
                            val lunch = convertGeneralCase(infos[2], regexForPrice)
                            val dinner = convertGeneralCase(infos[3], regexForPrice)

                            ret.addAll(breakfast.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.BREAKFAST, date) })
                            ret.addAll(lunch.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.LUNCH, date) })
                            ret.addAll(dinner.map { menuInfo -> Menu.of(restaurant[0]!!, menuInfo, Time.DINNER, date) })
                        }
                    }
            }
        }

        ret.forEach {
            // menu에 담긴 restaurantName으로 restaurant를 조회, restaurant_id를 가져온다.
            val restaurantId = getRestaurantId(it)

            // 가져온 restaurant_id를 foreign key로 설정해 menu 값을 db에 넣는다.
            insertMenu(it, restaurantId, date)
        }
    }

    fun deleteAll() {
        val deleteAllQuery = "DELETE FROM fake_menu WHERE id > 0;"
        val preparedStatement = connection.prepareStatement(deleteAllQuery)
        preparedStatement.executeUpdate()
        println("delete success")
    }

    private fun getRestaurantId(menu: Menu): Long {
        val getRestaurantIdQuery = "SELECT id FROM fake_restaurant WHERE name = '${menu.restaurantName}';"
        val preparedStatement = connection.prepareStatement(getRestaurantIdQuery)
        val resultSet = preparedStatement.executeQuery()

        if(resultSet.next()) {
            val restaurantId = resultSet.getLong("id")
            println("getRestaurantId success : $restaurantId, ${menu.restaurantName}")
            return restaurantId
        } else {
            throw Exception("getRestaurantId fail : ${menu.restaurantName}")
        }
    }

    private fun insertMenu(menu: Menu, restaurantId: Long, date: LocalDate) {
        println("insertMenu start : $menu, $restaurantId")

        val now = LocalDateTime.now()
        val insertMenuQuery = "INSERT INTO fake_menu (name, price, meal_time, msg, restaurant_id, date, is_valid, created_at, updated_at)" +
                " VALUES ('${menu.name?.trim()}', ${menu.price}, ${menu.time.ordinal}, '${menu.msg}', $restaurantId, '$date', ${menu.isValid}, '$now', '$now');"

        val preparedStatement = connection.prepareStatement(insertMenuQuery)
        preparedStatement.executeUpdate()
        println("insertMenu success : ${menu.name}")
    }


    private fun isGeneralCase(restaurantName: String?): Boolean {
        return restaurantName != null
                && restaurantName != "두레미담"
                && restaurantName != "4식당"
                && restaurantName != "301동식당"
                && restaurantName != "220동식당"
    }
}