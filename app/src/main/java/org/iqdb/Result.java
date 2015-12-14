package org.iqdb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Result implements Parcelable{
	/*
	 * Элементы результатов:
	 * 0 - исходное изображение
	 * 1 - найденый результат или результат не найден
	 * 2..последний - дополнительные результаты поиска
	 */
	public static final int INIT_IMAGE = 0;
	public static final int BEST_MATCH = 1;
	public static final int FOUND_RESULT = 2;
	public static final int RESULT_NOT_FOUND = 3;
	public static final int ADDITIONAL_RESULT = 4;
	public static final int POSSIBLE_RESULT = 5;
	
	
	public static final int DANBOORU = 10;
	public static final int KONACHAN = 11;
	public static final int YANDERE = 12;
	public static final int GELBOORU = 13;
	public static final int SANKAKU = 14;
	public static final int ESHUUSHUU = 15;
	public static final int THEANIMEGALLERY = 16;
	public static final int ZEROCHAN = 17;
	public static final int MANGADRAWING = 18;
	public static final int ANIMEPICTURES = 19;
	
	public String url;
	public String thumb;
	public int type;
	public int height = 0;
	public int width = 0;
	public int site = -1;
	public int similarity = 0;
	
	private static final Pattern imgUrlRegex = Pattern.compile("<a href=[\"'](.+?)[\"'][^>]*>");
	private static final Pattern rowRegex = Pattern.compile("<tr>(.+?)</tr>");
	private static final Pattern colRegex = Pattern.compile("<td>(.+?)</td>");
	//private static final Pattern sizeRegex = Pattern.compile("(\\d+)Ã(\\d+)");
	//private static final Pattern sizeRegex = Pattern.compile("(\\d+)×(\\d+)");
	private static final Pattern sizeRegex = Pattern.compile("(\\d+)[^\\d](\\d+)");
	private static final Pattern similarityRegex = Pattern.compile("(\\d+)% similarity");
	
	private static final Pattern thumbRegex = Pattern.compile("<img.+?src='(.+?)'[^>]*?>");
	
	
	public static Result parseHtml(String html){
		Result result = new Result();
		
		Matcher rowmatcher = rowRegex.matcher(html);
		List<String> rows = new ArrayList<String>();
		while(rowmatcher.find()){
			rows.add(rowmatcher.group(1));
		}
		
		String headerRow = rows.get(0);
		
		if(headerRow.contains("Best match")){
			result.type = BEST_MATCH;
		} else if(headerRow.contains("Possible match")){
			result.type = POSSIBLE_RESULT;
		} else if(headerRow.contains("Additional match")){
			result.type = ADDITIONAL_RESULT;
		} else if(headerRow.contains("No relevant matches")){
			result.type = RESULT_NOT_FOUND;
		} else if(headerRow.contains("Your image")){
			result.type = INIT_IMAGE;
		}

		if (result.type != RESULT_NOT_FOUND) {
			String urlHtml = rows.get(1);
			if (result.type != INIT_IMAGE) {
				Matcher urlmatcher = imgUrlRegex.matcher(urlHtml);
				if (urlmatcher.find()) {
					result.url = urlmatcher.group(1);
					if (result.url.contains("anime-pictures.net")) {
						result.site = ANIMEPICTURES;
					} else if (result.url.contains("mangadrawing.net")) {
						result.site = MANGADRAWING;
					} else if (result.url.contains("zerochan.net")) {
						result.site = ZEROCHAN;
					} else if (result.url.contains("theanimegallery.com")) {
						result.site = THEANIMEGALLERY;
					} else if (result.url.contains("gelbooru.com")) {
						result.site = GELBOORU;
					} else if (result.url.contains("yande.re")) {
						result.site = YANDERE;
					} else if (result.url.contains("konachan.com")) {
						result.site = KONACHAN;
					} else if (result.url.contains("danbooru.donmai.us")) {
						result.site = DANBOORU;
					} else if (result.url.contains("sankakucomplex.com")) {
						result.site = SANKAKU;
					} else if(result.url.contains("e-shuushuu.net")) {
						result.site = ESHUUSHUU;
					}
				}
				
				String similarityRow = rows.get(4);
				
				Matcher similarityMatcher = similarityRegex.matcher(similarityRow);
				if(similarityMatcher.find()){
					result.similarity = Integer.valueOf(similarityMatcher.group(1));
				}
				
			}
			
			Matcher thumbMatcher = thumbRegex.matcher(urlHtml);
			if(thumbMatcher.find()){
				result.thumb = "https://iqdb.org" + thumbMatcher.group(1);
			}
			if(rows.size() > 3){
			String sizeHtml = rows.get(3);
			
				Matcher sizeMatcher = sizeRegex.matcher(sizeHtml);
				if(sizeMatcher.find()){
					result.width = Integer.parseInt(sizeMatcher.group(1));
					result.height = Integer.parseInt(sizeMatcher.group(2));
				}
			}
		}

		/*
		switch (result.type) {
		case INIT_IMAGE:		
			break;
		case RESULT_NOT_FOUND:
			break;
		case FOUND_RESULT:
			break;
		case ADDITIONAL_RESULT:
			break;
		}
		*/
		
		
		//Initial image
		//0 - header
		//1 - thumb
		//2 - n/a
		//3 - size
		
		//Additional result
		//0 - header
		//1 - url/thumb
		//2 - site
		//3 - size
		//4 - similarity

		return result;
	}


	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}


	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[]{url, thumb});
		dest.writeIntArray(new int[]{type, height, width, site, similarity});
	}
	
	public Result(){}
	
	public Result(Parcel in){
		String[] strData = new String[2];
		int[] intData = new int[4];
		in.readStringArray(strData);
		in.readIntArray(intData);
		url = strData[0];
		thumb = strData[1];
		
		type = intData[0];
		height = intData[1];
		width = intData[2];
		site = intData[3];
		similarity = intData[4];
 	}
	
	public static final Parcelable.Creator<Result> CREATOR = new Creator<Result>() {

		public Result createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new Result(source);
		}

		public Result[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Result[size];
		}
		
	};
}
