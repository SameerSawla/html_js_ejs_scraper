import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ScrapeMindTap {

	public static Boolean isURL(String inputUrl) {

		URL url = null;
		try {
			url = new URL(inputUrl);
		} catch (MalformedURLException e) {

		}

		if (url == null) {
			return false;
		} else {
			return true;
		}
	}

	public static Boolean isNotEJSScript(String checkMe) {
		String pattern = "<%(.*?)%>";
		if (checkMe.matches(pattern)) {
			return false;
		} else {
			return true;
		}
	}

	public static String handleIframes(String stringText) {
		String pattern = "<iframe(.*?)/>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(stringText);

		while (m.find()) {
			String foundMatch = m.group();
			stringText = stringText.replace(foundMatch, "");
		}
		return stringText;
	}

	public static void processAttributeDetails(Element element, List<String> one) {
		if (element.tagName().equals("input")) {
			if (element.attr("type").equals("button")
					|| element.attr("type").equals("submit")
					|| element.attr("type").equals("text")) {
				String elementAttrValue = element.attr("value");
				if (!elementAttrValue.isEmpty()
						&& !one.contains(elementAttrValue)
						&& !isURL(elementAttrValue)
						&& !isAKeyword(elementAttrValue)) {
					one.add(elementAttrValue.trim());
				}
			}

			String elementAttrAlt = element.attr("alt");

			if (!elementAttrAlt.isEmpty() && !one.contains(elementAttrAlt)
					&& !isURL(elementAttrAlt) && !isAKeyword(elementAttrAlt)) {
				one.add(elementAttrAlt.trim());
			}

		} else {
			if (element.tagName().equals("a")) {
				String elementAttrTitleValue = element.attr("title");
					if (!elementAttrTitleValue.isEmpty()
							&& !one.contains(elementAttrTitleValue)
							&& !isURL(elementAttrTitleValue)
							&& !isAKeyword(elementAttrTitleValue)) {
						one.add(elementAttrTitleValue.trim());
					}
				

			} else {
				String elementAttrAlt = element.attr("alt");

				if (!elementAttrAlt.isEmpty() && !one.contains(elementAttrAlt)
						&& !isURL(elementAttrAlt)
						&& !isAKeyword(elementAttrAlt)) {
					one.add(elementAttrAlt);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void scrapeHTML(String filePath) {
		try {
			File file = new File(filePath);
			StringBuilder fileContents = new StringBuilder((int) file.length());
			try {
				@SuppressWarnings("resource")
				Scanner scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					fileContents.append(scanner.nextLine());
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String stringFile = fileContents.toString();
			stringFile = stringFile.replace("<strong>", "");
			stringFile = stringFile.replace("</strong>", "");
			List<String> one = new ArrayList<String>();
			Document doc = Jsoup.parse(stringFile);
			Elements elements = doc.getAllElements();

			for (Element element : elements) {
				processAttributeDetails(element, one);
				String elementText = element.ownText().trim();
				if (!elementText.isEmpty()) {
					if (!one.contains(elementText) && !isURL(elementText)
							&& isNotEJSScript(elementText)
							&& !one.contains(elementText)
							&& !isAKeyword(elementText)) {
						one.add(elementText);
					}
				}
			}

			JSONObject obj = new JSONObject();
			for (int i = 0; i < one.size(); i++) {
				obj.put(one.get(i), "");
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(obj.toJSONString());
			String prettyJsonString = gson.toJson(je);

			FileWriter convertedFile = new FileWriter(filePath + ".txt");
			convertedFile.write(prettyJsonString);
			convertedFile.flush();
			convertedFile.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("File " + filePath + " does not exist...");
			e.printStackTrace();
		}
	}

	public static void scrapeEJS(String filePath) {

		File file = new File(filePath);
		StringBuilder fileContents = new StringBuilder((int) file.length());
		try {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String stringFile = fileContents.toString();
		stringFile = stringFile.replace("<strong>", "");
		stringFile = stringFile.replace("</strong>", "");
		String pattern = "<%(.*?)%>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(stringFile);

		while (m.find()) {
			String foundMatch = m.group();
			stringFile = stringFile.replace(foundMatch, "");
		}

		stringFile = handleIframes(stringFile);
		scrapeHTMLFromString(stringFile, filePath);
	}

	@SuppressWarnings("unchecked")
	public static void scrapeHTMLFromString(String scrapeMe, String filePath) {

		try {
			List<String> one = new ArrayList<String>();
			scrapeMe = scrapeMe.replace("<strong>", "");
			scrapeMe = scrapeMe.replace("</strong>", "");
			Document doc = Jsoup.parse(scrapeMe);
			Elements elements = doc.getAllElements();

			for (Element element : elements) {
				if (!element.tagName().toLowerCase().equals("noscript")) {

					processAttributeDetails(element, one);
					String elementText = element.ownText().trim();
					if (!elementText.isEmpty()) {
						if (!one.contains(elementText) && !isURL(elementText)
								&& isNotEJSScript(elementText)
								&& !isAKeyword(elementText)) {
							one.add(elementText);
						}
					}
				}
			}

			if (!one.isEmpty()) {

				JSONObject obj = new JSONObject();
				for (int i = 0; i < one.size(); i++) {
					obj.put(one.get(i), "");
				}

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(obj.toJSONString());
				String prettyJsonString = gson.toJson(je);

				FileWriter convertedFile = new FileWriter(filePath + ".txt");
				convertedFile.write(prettyJsonString);
				convertedFile.flush();
				convertedFile.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public static void scrapeJS(String filePath) {
		ArrayList<String> one = new ArrayList<String>();
		File file = new File(filePath);

		try {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) {
				// fileContents.append(scanner.nextLine());
				String computed = regexComputationJS(scanner.nextLine());
				if (!computed.isEmpty() && !one.contains(computed)
						&& !isAKeyword(computed)) {
					one.add(computed);
				}
			}

			JSONObject obj = new JSONObject();
			for (int i = 0; i < one.size(); i++) {
				obj.put(one.get(i), "");
			}

			if (one.size() > 0) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(obj.toJSONString());
				String prettyJsonString = gson.toJson(je);

				FileWriter convertedFile = new FileWriter(filePath + ".txt");
				convertedFile.write(prettyJsonString);
				convertedFile.flush();
				convertedFile.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Boolean isAKeyword(String testMe) {
		ArrayList<String> tempArrayList = populateIDontNeedArray();

		if (testMe.contains("\"")) {
			testMe = testMe.substring(1, testMe.length() - 1);
		}

		if (tempArrayList.contains(testMe)) {
			return true;
		}
		return false;
	}

	public static ArrayList<String> populateIDontNeedArray() {
		ArrayList<String> IDontNeedArray = new ArrayList<String>();

		IDontNeedArray.add("url");
		IDontNeedArray.add("get");
		IDontNeedArray.add("json");
		IDontNeedArray.add("put");
		IDontNeedArray.add("text");
		IDontNeedArray.add("delete");
		IDontNeedArray.add("post");
		IDontNeedArray.add("+");
		IDontNeedArray.add("-");
		IDontNeedArray.add("*");
		IDontNeedArray.add("\\");
		IDontNeedArray.add("?");
		return IDontNeedArray;
	}

	public static Boolean areAllCharacters(String testMe) {
		String specialChars = "=/*!@#$%^&*()\"{}_[]|\\?/<>,.";
		char[] charArray = testMe.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			String temp = "" + charArray[i];
			if (!specialChars.contains(temp)) {
				return false;
			}
		}
		return true;
	}

	public static Boolean isPath(String testMe) {
		if (testMe.endsWith("/")) {
			return true;
		}

		if (testMe.startsWith("/")) {
			return true;
		}

		String pattern = ".*?/.*?";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(testMe);
		while (m.find()) {
			return true;
		}

		return false;
	}

	public static String regexComputationJS(String stringFile) {

		String pattern = "=\\s*\".*?\"\\s*;";
		String pattern2 = "=\\s*\".*?\"\\s*\\+";

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(stringFile);

		Pattern p2 = Pattern.compile(pattern2);

		while (m.find()) {
			String matchedString = m.group();
			Matcher m2 = p2.matcher(matchedString);

			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);

				Pattern pTemp = Pattern.compile(".*\\\"(.*)\\\".*");
				Matcher mTemp = pTemp.matcher(matchedString);

				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				}

				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						return matchedString;

					}
				}
			}
		}

		pattern = "=\\s*\".*?\"\\s*,";
		pattern2 = "=\\s*\".*?\"\\s*\\+";

		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		p2 = Pattern.compile(pattern2);

		while (m.find()) {
			String matchedString = m.group();
			Matcher m2 = p2.matcher(matchedString);

			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);

				Pattern pTemp = Pattern.compile(".*\\\"(.*)\\\".*");
				Matcher mTemp = pTemp.matcher(matchedString);

				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				}

				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						return matchedString;

					}
				}
			}
		}

		pattern = "=\\s*'.*?'\\s*,";
		pattern2 = "=\\s*'.*?'\\s*\\+";

		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		p2 = Pattern.compile(pattern2);

		while (m.find()) {
			String matchedString = m.group();
			Matcher m2 = p2.matcher(matchedString);

			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);
				Pattern pTemp = Pattern.compile(".*\\'(.*)\\'.*");
				Matcher mTemp = pTemp.matcher(matchedString);

				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				}
				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						return matchedString;

					}
				}
			}
		}

		pattern = "=\\s*'.*?'\\s*;";
		pattern2 = "=\\s*'.*?'\\s*\\+";

		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		p2 = Pattern.compile(pattern2);

		while (m.find()) {
			String matchedString = m.group();
			Matcher m2 = p2.matcher(matchedString);

			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);
				Pattern pTemp = Pattern.compile(".*\\'(.*)\\'.*");
				Matcher mTemp = pTemp.matcher(matchedString);

				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				}
				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						return matchedString;

					}
				}
			}
		}

		pattern = ".html(\\s*\".*?\"\\s*)\\s*;";
		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		while (m.find()) {
			String matchedString = m.group();
			if (!matchedString.isEmpty() || !matchedString.equals("")) {
				if (!isPath(matchedString) && !isAKeyword(matchedString)) {

					matchedString = matchedString.replaceAll(".*\\(|\\).*", "");
					if (!matchedString.equals("\"\"")) {
						matchedString = matchedString.substring(1,
								matchedString.length() - 1);
						if (!isPath(matchedString)
								&& !isAKeyword(matchedString)) {
							return matchedString;
						}

					}
				}

			}
		}

		pattern = ".text\\(\\s*\".*?\"\\s*\\)";
		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		while (m.find()) {
			String matchedString = m.group();
			if (!matchedString.isEmpty() || !matchedString.equals("")) {
				if (!isPath(matchedString) && !isAKeyword(matchedString)) {

					matchedString = matchedString.replaceAll(".*\\(|\\).*", "");
					if (!matchedString.equals("\"\"")) {
						matchedString = matchedString.substring(1,
								matchedString.length() - 1);
						if (!isPath(matchedString)
								&& !isAKeyword(matchedString)) {
							return matchedString;
						}

					}
				}
			}
		}

		pattern = ".html\\(\\s*'.*?'\\s*\\)\\s*;";
		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		while (m.find()) {
			String matchedString = m.group();
			if (!matchedString.isEmpty() || !matchedString.equals("")) {
				matchedString = matchedString.replaceAll(".*\\(|\\).*", "");
				if (!matchedString.equals("''")) {
					matchedString = matchedString.substring(1,
							matchedString.length() - 1);
					if (!isPath(matchedString) && !isAKeyword(matchedString)) {
						return matchedString;
					}

				}
			}
		}

		pattern = ".text\\(\\s*'.*?'\\s*\\)";
		p = Pattern.compile(pattern);
		m = p.matcher(stringFile);

		while (m.find()) {
			String matchedString = m.group();
			if (!matchedString.isEmpty() || !matchedString.equals("")) {
				if (!isPath(matchedString) && !isAKeyword(matchedString)) {

					matchedString = matchedString.replaceAll(".*\\(|\\).*", "");
					if (!matchedString.equals("''")) {
						matchedString = matchedString.substring(1,
								matchedString.length() - 1);
						if (!isPath(matchedString)
								&& !isAKeyword(matchedString)) {
							// System.out.println(matchedString);
							return matchedString;
						}

					}
				}
			}
		}

		pattern = "alert\\((.*)\\)";
		pattern2 = "alert\\(\\\".*\\\"+.*\\)";

		p = Pattern.compile(pattern);
		p2 = Pattern.compile(pattern2);
		m = p.matcher(stringFile);

		if (m.find()) {
			String matchedString = m.group(1);
			Matcher m2 = p2.matcher(stringFile);
			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);

				Pattern pTemp = Pattern.compile("\\\"(.*)\\\"");
				Matcher mTemp = pTemp.matcher(matchedString);
				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				} else {
					pTemp = Pattern.compile("'(.*)'");
					mTemp = pTemp.matcher(matchedString);
					if (mTemp.find()) {
						matchedString = mTemp.group(1);
					}
				}

				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						// System.out.println(matchedString);
						return matchedString;
					}
				}
			}
		}

		pattern = "alert\\('(.*)'\\)";
		pattern2 = "alert\\('.*'+.*\\)";

		p = Pattern.compile(pattern);
		p2 = Pattern.compile(pattern2);
		m = p.matcher(stringFile);

		if (m.find()) {
			String matchedString = m.group(1);
			Matcher m2 = p2.matcher(stringFile);
			ArrayList<String> subParts = new ArrayList<String>();

			subParts.add(matchedString);

			if (m2.find()) {
				subParts = returnSubParts2(matchedString);
			}

			for (int i = 0; i < subParts.size(); i++) {
				matchedString = subParts.get(i);

				Pattern pTemp = Pattern.compile("'(.*)'");
				Matcher mTemp = pTemp.matcher(matchedString);
				if (mTemp.find()) {
					matchedString = mTemp.group(1);
				}

				if (!matchedString.isEmpty()) {
					if (!isPath(matchedString) && !isAKeyword(matchedString)
							&& !areAllCharacters(matchedString)) {
						// System.out.println(matchedString);
						return matchedString;
					}
				}
			}
		}

		return "";
	}

	public static ArrayList<String> returnSubParts(String splitMe) {
		ArrayList<String> returnMe = new ArrayList<String>();
		String[] splitted = splitMe.split("\\+");
		for (int i = 0; i < splitted.length; i++) {
			String pattern = "\".*?\"";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(splitted[i]);
			if (m.find()) {
				returnMe.add(m.group());
			}
		}

		return returnMe;
	}

	public static ArrayList<String> returnSubParts2(String splitMe) {
		ArrayList<String> returnMe = new ArrayList<String>();
		String[] splitted = splitMe.split("\\+");
		for (int i = 0; i < splitted.length; i++) {
			String pattern = "'.*?'";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(splitted[i]);
			if (m.find()) {
				returnMe.add(m.group());
			}
		}

		return returnMe;
	}

	public static String getExtension(String input) {
		if (input.endsWith(".html")) {
			return "html";
		}
		if (input.endsWith(".ejs")) {
			return "ejs";
		}
		if (input.endsWith(".js")) {
			return "js";
		}
		return null;
	}

	public static void recurseIt(File[] listOfFiles,
			ArrayList<String> htmlPaths, ArrayList<String> ejsPaths,
			ArrayList<String> jsPaths) {
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String path = listOfFiles[i].getAbsolutePath();
				if (getExtension(path) != null) {
					if (getExtension(path).equals("html")) {
						htmlPaths.add(path);
					}
					if (getExtension(path).equals("ejs")) {
						ejsPaths.add(path);
					}
					if (getExtension(path).equals("js")) {
						jsPaths.add(path);
					}
				}

			} else {
				if (listOfFiles[i].isDirectory()) {
					File folder = new File(listOfFiles[i].getAbsolutePath());
					File[] listOfFilesInner = folder.listFiles();
					recurseIt(listOfFilesInner, htmlPaths, ejsPaths, jsPaths);
				}
			}
		}
	}

	public static void main(String args[]) {
		ArrayList<String> htmlPaths = new ArrayList<String>();
		ArrayList<String> ejsPaths = new ArrayList<String>();
		ArrayList<String> jsPaths = new ArrayList<String>();
		// System.out.println(args[1]);
		// File folder = new
		// File("/home/sameer/Repos/ng-ui/src/iloveapps/rssfeed");
		File folder = new File(
				"/home/sameer/Repos/ng-ui/src/iloveapps/webvideo/apps/webvideo");

		// File tempFile = new
		// File("/home/sameer/Repos/ng-ui/src/iloveapps/webvideo/apps/webvideo/help/results.ejs");

		File[] listOfFiles = folder.listFiles();

		recurseIt(listOfFiles, htmlPaths, ejsPaths, jsPaths);

		for (String htmlPathElements : htmlPaths) {
			scrapeHTML(htmlPathElements);
		}
		for (String ejsPathElements : ejsPaths) {
			scrapeEJS(ejsPathElements);
		}
		for (String jsPathElements : jsPaths) {
			scrapeJS(jsPathElements);
		}

	}
}
