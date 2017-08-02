package org.klebertiko

object Krawler {

    @JvmStatic fun main(args: Array<String>) {
        val spider = KSpider()
        spider.search("http://www.concrete.com.br", "desenvolvedor")
    }
}