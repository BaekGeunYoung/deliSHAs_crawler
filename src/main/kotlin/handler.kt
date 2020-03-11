import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.jsoup.Jsoup

class Crawler: RequestHandler<Req, Unit> {
    override fun handleRequest(input: Req?, context: Context) {
        val logger = context.logger

    }

    fun crawl() {
        Jsoup.connect("http://snuco.snu.ac.kr/ko/foodmenu").get().run {
            select("table.views-table cols-4")
                .select("tbody")
                .select("tr")
                .forEach {
                    val infos = it.select("td")
                    val restaurantName = infos[0]
                    val breakfast = infos[1].select("p").map { p -> p.text() }
                    val lunch = infos[2].select("p").map { p -> p.text() }
                    val dinner = infos[3].select("p").map { p -> p.text() }
            }
        }
    }
}