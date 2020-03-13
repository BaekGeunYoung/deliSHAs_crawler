import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {
    val crawler = Crawler()

    val formatter = DateTimeFormatter.ofPattern("MM/dd/YYYY")
    val today = LocalDate.now()

    val baseUrl = Constants.BASE_CRAWL_URL

    for (i in (0 until 7)) {
        val date = today.plusDays(i.toLong())
        val day = formatter.format(date)
        val url = "$baseUrl?field_menu_date_value_1[value][date]=&field_menu_date_value[value][date]=$day"

        crawler.crawl(url, date)
    }
}