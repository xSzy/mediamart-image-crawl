package com.example.webcrawler.mediamartdemo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
	
	String baseFolder = "C:\\MediamartCrawler\\";
	public void crawl(String url)
	{
		try {
			Document doc = Jsoup.connect(url).get();
			Element body = doc.body();
			String templateUrl = "url" + "?&trang=";
			int pageCount = 1;
			//get pagination
			Element pageItem = body.getElementById("pagination");
			if(pageItem == null)	//no pageitem
			{
				System.out.println("Page not found, will find only in this page");
			}
			else
			{
				//counting page
				int pagesize = Integer.parseInt(pageItem.attr("data-pagesize"));
				int currentpage = Integer.parseInt(pageItem.attr("data-pagecurrent"));
				int datasize = Integer.parseInt(pageItem.attr("data-total"));
				pageCount = (datasize % pagesize == 0) ? datasize/pagesize : datasize/pagesize+1;
				templateUrl = pageItem.attr("data-link");
			}
			
			//loop for every page
			for(int page = 1; page <= pageCount; page++)
			{
				Document pageDoc = Jsoup.connect(templateUrl + page).get();
				System.out.println("Crawling in page " + page);
				crawlPage(pageDoc);
			}
			
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void crawlPage(Document doc)
	{
		Element body = doc.body();
		//crawl item on page
		Elements items = body.getElementsByClass("pl18-item-li");
		for(Element item : items)
		{
//			System.out.println(item.text());
			crawlItem(item);
		}
	}

	private void crawlItem(Element item) {
		Elements images = item.getElementsByClass("pl18-item-image");
		for(Element image : images)
		{
			String url = image.getElementsByTag("a").attr("abs:href");
//			System.out.println(url);
			try {
				Document doc = Jsoup.connect(url).get();
//				System.out.println(doc.title());
//				System.out.println(doc.toString());
				Element nameDiv = doc.getElementsByClass("pdtr-name").get(0);
				String name = nameDiv.getElementsByTag("span").get(0).html();
				name = validateFilename(name);
				System.out.println(name);
				//create folder
//				File folder = new File(baseFolder + name + "\\");
//				if(!folder.mkdir())
//				{
//					System.out.println("Folder cannot be created!");
//					continue;
//				}
				//create txt file, with product name as filename and product images as content
				File file = new File(baseFolder + name + ".txt");
				if(!file.createNewFile())
				{
					System.out.println("Error while creating file, skipping...");
					continue;
				}
				FileWriter writer = new FileWriter(file);
				
				Elements imagesDiv = doc.getElementsByClass("g_item");
//				System.out.println(imagesDiv.size());
				for(Element imageDiv : imagesDiv)
				{
					//download with imgUrl
					System.out.println("Downloading image " + (imagesDiv.indexOf(imageDiv)+1) + " of " + imagesDiv.size() + "...");
					List<String> imageUrlList = imageDiv.getElementsByTag("a").eachAttr("data-img");
					for(String imageUrl : imageUrlList)
					{
//						String imgUrl = imageUrl.replace("thumb_", "");
						URL u = new URL(imageUrl);
						InputStream in = new BufferedInputStream(u.openStream());
						String filepath = baseFolder + getFilenameFromUrl(imageUrl);
						OutputStream out = new BufferedOutputStream(new FileOutputStream(filepath));

						for(int i; (i = in.read()) != -1;) {
						    out.write(i);
						}
						in.close();
						out.close();
						
						//convert each image to jpg if they are png
						if(getFileExtension(filepath).equals("jpg") || getFileExtension(filepath).equals("jpeg"))
						{
							writer.append(getFilenameFromUrl(imageUrl) + "\n");
							continue;
						}
						convertToJpg(filepath);
						writer.append(getFilenameFromUrl(imageUrl).replace(getFileExtension(imageUrl), "jpg") + "\n");
					}
				}
				
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void convertToJpg(String filepath) {
		BufferedImage bufferedImage;

		try {

			// read image file
			bufferedImage = ImageIO.read(new File(filepath));

			// create a blank, RGB, same width and height, and a white background
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

			// write to jpeg file
			ImageIO.write(newBufferedImage, "jpg", new File(filepath.replace(getFileExtension(filepath), "jpg")));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFilenameFromUrl(String url)
	{
		int index = url.lastIndexOf("/");
		if(index == -1)
			return null;
		return url.substring(index+1, url.length());
	}
	
	private String getFileExtension(String path)
	{
		int index = path.lastIndexOf(".");
		if(index == -1)
			return null;
		return path.substring(index+1, path.length());
	}
	
	private String validateFilename(String filename)
	{
		for(char i : ILLEGAL_CHARACTERS)
		{
			filename = filename.replace(Character.toString(i), "");
		}
		return filename;
	}
}
