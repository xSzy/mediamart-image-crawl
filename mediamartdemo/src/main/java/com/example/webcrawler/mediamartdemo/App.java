package com.example.webcrawler.mediamartdemo;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String url = "https://mediamart.vn/laptop/";
        WebCrawler crawler = new WebCrawler();
        crawler.crawl(url);
    }
}
