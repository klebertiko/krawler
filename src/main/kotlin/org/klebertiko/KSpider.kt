package org.klebertiko

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*

class KSpider {

    private val pagesVisited = HashSet<String>()
    private val pagesToVisit = LinkedList<String>()
    private val links = LinkedList<String>()
    private var htmlDocument: Document? = null

    /**
     * Our main launching point for the Spider's functionality. Internally it creates spider legs
     * that make an HTTP request and parse the response (the web page).

     * @param url        - The starting point of the spider
     * *
     * @param searchWord - The word or string that you are searching for
     */
    fun search(url: String, searchWord: String) {

        while (this.pagesVisited.size < MAX_PAGES_TO_SEARCH) {
            val currentUrl: String

            if (this.pagesToVisit.isEmpty()) {
                currentUrl = url
                this.pagesVisited.add(url)
            } else {
                currentUrl = this.nextUrl()
            }

            this.crawl(currentUrl) // Lots of stuff happening here. Look at the crawl method in
            // SpiderLeg
            val success = this.searchForWord(searchWord)

            if (success) {
                println(String.format("**Success** Word %s found at %s", searchWord, currentUrl))
                break
            }
            this.pagesToVisit.addAll(this.getLinks())
        }

        println("\n**Done** Visited " + this.pagesVisited.size + " web page(s)")
    }

    /**
     * Returns the next URL to visit (in the order that they were found). We also do a check to make
     * sure this method doesn't return a URL that has already been visited.

     * @return
     */
    private fun nextUrl(): String {
        var nextUrl: String
        do {
            nextUrl = this.pagesToVisit.removeAt(0)
        } while (this.pagesVisited.contains(nextUrl))
        this.pagesVisited.add(nextUrl)
        return nextUrl
    }

    /**
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl

     * @param url - The URL to visit
     * *
     * @return whether or not the crawl was successful
     */
    fun crawl(url: String): Boolean {
        try {

            val connection = Jsoup.connect(url).userAgent(USER_AGENT)
            val htmlDocument = connection.get()
            this.htmlDocument = htmlDocument

            // 200 is the HTTP OK status code indicating that everything is great.
            if (connection.response().statusCode() == 200) {
                println("\n**Visiting** Received web page at " + url)
            }

            if (!connection.response().contentType().contains("text/html")) {
                println("**Failure** Retrieved something other than HTML")
                return false
            }

            val linksOnPage = htmlDocument.select("a[href]")
            println("Found (" + linksOnPage.size + ") links")

            for (link in linksOnPage) {
                this.links.add(link.absUrl("href"))
            }
            return true
        } catch (ioe: IOException) {
            // We were not successful in our HTTP request
            return false
        }

    }

    /**
     * Performs a search on the body of on the HTML document that is retrieved. This method should
     * only be called after a successful crawl.

     * @param searchWord - The word or string to look for
     * *
     * @return whether or not the word was found
     */
    fun searchForWord(searchWord: String): Boolean {
        // Defensive coding. This method should only be used after a successful crawl.
        if (this.htmlDocument == null) {
            println("ERROR! Call crawl() before performing analysis on the document")
            return false
        }

        println("Searching for the word $searchWord...")
        val bodyText = this.htmlDocument!!.body().text()
        return bodyText.toLowerCase().contains(searchWord.toLowerCase())
    }

    fun getLinks(): List<String> {
        return this.links
    }

    companion object {

        private val MAX_PAGES_TO_SEARCH = 10

        // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
        private val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1"
    }
}
