import Parser.Companion.convert301
import Parser.Companion.convert4Sicdang
import Parser.Companion.convertDuremidam
import Parser.Companion.convertGeneralCase
import Parser.Companion.separateContact
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Crawler: RequestHandler<Any, Unit> {
    private val dataSource = DataSource()
    private val connection = dataSource.getConnection()
    private lateinit var logger: LambdaLogger

    override fun handleRequest(input: Any?, context: Context) {
        logger = context.logger
        val formatter = DateTimeFormatter.ofPattern("MM/dd/YYYY")
        val today = LocalDate.now()

        val baseUrl = Constants.BASE_CRAWL_URL

        // 이전에 저장돼있던 메뉴들을 모두 삭제한다.
        deleteAll()

        for (i in (0 until Constants.CRAWL_DAYS_UNTIL)) {
            val date = today.plusDays(i.toLong())
            val day = formatter.format(date)
            val url = "$baseUrl?field_menu_date_value_1[value][date]=&field_menu_date_value[value][date]=$day"

            printAndLog("crawling start : $day")
            crawl(url, date)
            printAndLog("==========================================================================")
        }

        //WAS의 도메인 및 SSL이 나오면 그때 활성화시키자.
        //sendRefreshRequest()
    }

    fun sendRefreshRequest() {
        val url = URL("${Constants.WAS_URL}/api/v1/restaurants/")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            println("Sent refresh request to URL : $url, Response Code : $responseCode")
        }
    }

    fun crawl(url: String, date: LocalDate) {
        insertRestaurant(date)

        val ret = mutableListOf<Menu>()

        Jsoup.connect(url).get().run {
            select("table.views-table tbody")
                .select("tr")
                .forEach  lit@{
                    val infos = it.select("td")
                    if (infos.size != 4) return@lit

                    val restaurant = separateContact(infos[0].text())

                    when {
                        isFixedCase(restaurant[0]) -> {
                            val lunch = Menu(
                                restaurantName = restaurant[0]!!,
                                time = Time.LUNCH,
                                date = date,
                                name = infos[2].select("p")[0].text(),
                                price = null,
                                msg = null,
                                isValid = true
                            )

                            val dinner = Menu(
                                restaurantName = restaurant[0]!!,
                                time = Time.DINNER,
                                date = date,
                                name = infos[3].select("p")[0].text(),
                                price = null,
                                msg = null,
                                isValid = true
                            )

                            ret.add(lunch)
                            ret.add(dinner)
                        }
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

    private fun insertRestaurant(date: LocalDate) {
        val getRestaurantInfoIdQuery = "SELECT id FROM restaurant_info;"
        var preparedStatement = connection.prepareStatement(getRestaurantInfoIdQuery)
        val resultSet = preparedStatement.executeQuery()
        val now = LocalDateTime.now()
        var insertRestaurantQuery = "INSERT INTO restaurant (date, restaurant_info_id, created_at, updated_at) VALUES "
        while (resultSet.next()) {
            val restaurantInfoId = resultSet.getLong("id")
            insertRestaurantQuery += "('$date', $restaurantInfoId, '$now', '$now'), "
        }

        insertRestaurantQuery = insertRestaurantQuery.substring(0, insertRestaurantQuery.length - 2)

        preparedStatement = connection.prepareStatement(insertRestaurantQuery)
        preparedStatement.executeUpdate()

        printAndLog("insert Restaurant success of following date : $date")
    }

    fun deleteAll() {
        deleteAllMenus()
        deleteAllRestaurant()
    }

    private fun deleteAllMenus() {
        val deleteAllQuery = "DELETE FROM menu WHERE id > 0;"
        val preparedStatement = connection.prepareStatement(deleteAllQuery)
        preparedStatement.executeUpdate()
        printAndLog("delete menus success")
    }

    private fun deleteAllRestaurant() {
        val deleteAllQuery = "DELETE FROM restaurant WHERE id > 0;"
        val preparedStatement = connection.prepareStatement(deleteAllQuery)
        preparedStatement.executeUpdate()
        printAndLog("delete restaurant success")
    }

    private fun getRestaurantId(menu: Menu): Long {
        val getRestaurantInfoIdQuery = "SELECT id FROM restaurant_info WHERE name = '${menu.restaurantName}';"
        var preparedStatement = connection.prepareStatement(getRestaurantInfoIdQuery)
        val restaurantInfoIdSet = preparedStatement.executeQuery()

        if(restaurantInfoIdSet.next()) {
            val restaurantInfoId = restaurantInfoIdSet.getLong("id")
            val getRestaurantIdQuery = "SELECT id FROM restaurant WHERE restaurant_info_id = '$restaurantInfoId';"
            preparedStatement = connection.prepareStatement(getRestaurantIdQuery)
            val restaurantIdSet = preparedStatement.executeQuery()

            if (restaurantIdSet.next()) {
                val restaurantId = restaurantIdSet.getLong("id")
                printAndLog("getRestaurantId success : $restaurantId, ${menu.restaurantName}")
                return restaurantId
            } else {
                throw Exception("getRestaurantId fail : ${menu.restaurantName}")
            }
        } else {
            throw Exception("getRestaurantId fail : ${menu.restaurantName}")
        }
    }

    private fun insertMenu(menu: Menu, restaurantId: Long, date: LocalDate) {
        printAndLog("insertMenu start : $menu, $restaurantId")

        val now = LocalDateTime.now()
        val insertMenuQuery = "INSERT INTO menu (name, price, meal_time, msg, restaurant_id, date, is_valid, created_at, updated_at)" +
                " VALUES ('${menu.name?.trim()}', ${menu.price}, '${menu.time.name}', '${menu.msg?.trim()}', $restaurantId, '$date', ${menu.isValid}, '$now', '$now');"

        val preparedStatement = connection.prepareStatement(insertMenuQuery)
        preparedStatement.executeUpdate()
        printAndLog("insertMenu success : ${menu.name}")
    }

    private fun isFixedCase(restaurantName: String?): Boolean {
        return restaurantName != null
                && (restaurantName == "라운지오"
                || restaurantName == "샤반"
                || restaurantName == "소담마루")
    }

    private fun isGeneralCase(restaurantName: String?): Boolean {
        return restaurantName != null
                && restaurantName != "두레미담"
                && restaurantName != "4식당"
                && restaurantName != "301동식당"
                && restaurantName != "220동식당"
    }

    private fun printAndLog(str: String) {
        println(str)
//        logger.log(str)
    }
}