package com.tangqiang.web.capture;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBaijia {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private URL url = getClass().getResource("/");

	public void execute() {
		System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("window-size=1200,800");
		WebDriver driver = new ChromeDriver(options);
		try {
			driver.manage().window().maximize();
			driver.get("https://baijia.baidu.com/channel?cat=1");

			savePage(driver, url.getPath(), "baijia_news_all" + System.currentTimeMillis() + ".png");

			WebElement element = driver.findElements(By.className("article-info")).get(0);
			saveElement(driver, element, url.getPath(), "baijia_news" + System.currentTimeMillis() + ".png");

		} catch (Exception e) {
			logger.error("TestBaijia error !", e);
		} finally {
			driver.close();
			driver.quit();
		}
	}

	private void savePage(WebDriver driver, String filePath, String fileName) throws IOException {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		jsExecutor.executeScript("window.scrollTo(0,0)");
		BufferedImage imageOriginal = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);// 创建全屏截图
		int lastScroll = -1;
		int currentScroll = 0;
		while (lastScroll != currentScroll) {
			byte[] bytesScroll = takesScreenshot.getScreenshotAs(OutputType.BYTES);
			BufferedImage imageScroll = ImageIO.read(new ByteArrayInputStream(bytesScroll));
			int screenHeight = imageScroll.getHeight();
			int screenWidth = imageScroll.getWidth();

			BufferedImage combined = new BufferedImage(screenWidth, currentScroll + screenHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = combined.getGraphics();
			g.drawImage(imageOriginal, 0, 0, null);
			g.drawImage(imageScroll, 0, currentScroll, null);
			imageOriginal = combined;

			logger.info("lastScroll:" + lastScroll + "    currentScroll:" + currentScroll + "    screenHeight:" + screenHeight);
			int scrollTo = currentScroll + screenHeight;
			lastScroll = currentScroll;
			jsExecutor.executeScript("window.scrollTo(0," + scrollTo + ")");
			currentScroll = Double.valueOf(jsExecutor.executeScript("return document.body.scrollTop").toString()).intValue();
		}

		File path = new File(filePath);
		if (!path.exists() || !path.isDirectory()) {
			path.mkdirs();
		}
		File file = new File(path.getAbsolutePath() + File.separatorChar + fileName);
		logger.info("File Path:" + file.getAbsolutePath());
		ImageIO.write(imageOriginal, "png", file);
	}

	/**
	 * 保存Element至文件
	 */
	private void saveElement(WebDriver driver, WebElement element, String filePath, String fileName) throws IOException {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		Point point = element.getLocation();
		Dimension dimension = element.getSize();

		int scrollTo = (point.y - 5);
		jsExecutor.executeScript("window.scrollTo(0, " + (scrollTo) + ")");
		int currentScroll = Double.valueOf(jsExecutor.executeScript("return document.body.scrollTop").toString()).intValue();
		logger.info("Element Point:" + point + "	Size:" + dimension + "	ScrollTo:" + scrollTo + "	Scroll:" + currentScroll + "	fileName:" + fileName);
		byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		// 创建全屏截图
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bytes));
		// 截取webElement所在位置的子图。
		BufferedImage croppedImage = originalImage.getSubimage(point.x - 4, point.y - currentScroll - 4, dimension.width + 8, dimension.height + 8);
		File path = new File(filePath);
		if (!path.exists() || !path.isDirectory()) {
			path.mkdirs();
		}
		File file = new File(path.getAbsolutePath() + File.separatorChar + fileName);
		logger.info("File Path:" + file.getAbsolutePath());
		ImageIO.write(croppedImage, "png", file);

		File fileFull = new File(path.getAbsolutePath() + File.separatorChar + "full_" + fileName);
		ImageIO.write(originalImage, "png", fileFull);
	}
}
