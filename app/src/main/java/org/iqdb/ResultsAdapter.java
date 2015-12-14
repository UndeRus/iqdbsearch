package org.iqdb;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ResultsAdapter extends ArrayAdapter<Result> {
	private DisplayImageOptions options;
	private ImageLoader imLoader;

	public ResultsAdapter(Context context) {
		super(context, 0);
		// TODO Auto-generated constructor stub
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.iqdb)
                .showImageForEmptyUri(R.drawable.iqdb)
                .showImageOnFail(R.drawable.iqdb)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        imLoader = ImageLoader.getInstance();
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Result result = getItem(position);
		View v = convertView;
		if(v == null || !(v instanceof RelativeLayout)){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			v = inflater.inflate(R.layout.result, null);
			
			
			ViewHolder vh = new ViewHolder();
			
			vh.imgThumb = (ImageView)v.findViewById(R.id.imgThumb);
			vh.txtTitle = (TextView)v.findViewById(R.id.txtTitle);
			vh.txtSize = (TextView)v.findViewById(R.id.txtSize);
			vh.wrapper = (RelativeLayout)v.findViewById(R.id.wrapper);
			vh.imgFavicon = (ImageView)v.findViewById(R.id.imgFavicon);
			vh.txtSimilarity = (TextView)v.findViewById(R.id.txtSimilarity);
			
			v.setTag(vh);
		}


        Resources res = getContext().getResources();
		
		ViewHolder vh = (ViewHolder)v.getTag();
		vh.txtTitle.setVisibility(View.VISIBLE);
		vh.txtSize.setVisibility(View.VISIBLE);
		vh.imgThumb.setVisibility(View.VISIBLE);
		vh.imgFavicon.setVisibility(View.VISIBLE);
		vh.txtSimilarity.setVisibility(View.VISIBLE);
		
		switch (result.type) {
		case Result.INIT_IMAGE:
			vh.txtTitle.setText(R.string.uploaded);
			vh.imgFavicon.setVisibility(View.GONE);
			vh.txtSimilarity.setVisibility(View.GONE);
			break;
		case Result.BEST_MATCH:
			vh.txtTitle.setText(R.string.best);
			vh.wrapper.setBackgroundColor(Color.parseColor("#FF8000"));
			vh.txtSimilarity.setText(res.getString(R.string.similarity_value, result.similarity));
			break;
		case Result.POSSIBLE_RESULT:
			vh.txtTitle.setText(R.string.possible);
			vh.txtSimilarity.setText(res.getString(R.string.similarity_value, result.similarity));
			break;
		case Result.RESULT_NOT_FOUND:
			vh.txtSize.setVisibility(View.GONE);
			vh.txtTitle.setText(R.string.not_found);
			vh.imgFavicon.setVisibility(View.GONE);
			vh.imgThumb.setVisibility(View.GONE);
			vh.txtSimilarity.setVisibility(View.GONE);
			break;
		case Result.FOUND_RESULT:
			vh.txtTitle.setText(R.string.result);
			vh.txtSimilarity.setText(res.getString(R.string.similarity_value, result.similarity));
			break;
		case Result.ADDITIONAL_RESULT:
			vh.txtTitle.setText(R.string.additional);
			vh.txtSimilarity.setText(res.getString(R.string.similarity_value, result.similarity));
			break;
		}
		
		if(result.type != Result.RESULT_NOT_FOUND){
			if(result.width == 0 || result.height == 0){
				vh.txtSize.setVisibility(View.GONE);
			} else {
				vh.txtSize.setText(res.getString(R.string.size_value, result.width, result.height));
			}
			
			
			switch (result.site) {
			case Result.DANBOORU:
				vh.imgFavicon.setImageResource(R.drawable.danbooru);
				break;
			case Result.KONACHAN:
				vh.imgFavicon.setImageResource(R.drawable.konachan);
				break;
			case Result.YANDERE:
				vh.imgFavicon.setImageResource(R.drawable.yandere);
				break;
			case Result.GELBOORU:
				vh.imgFavicon.setImageResource(R.drawable.gelbooru);
				break;
			case Result.SANKAKU:
				vh.imgFavicon.setImageResource(R.drawable.sankaku);
				break;
			case Result.ESHUUSHUU:
				vh.imgFavicon.setImageResource(R.drawable.eshuushuu);
				break;
			case Result.THEANIMEGALLERY:
				vh.imgFavicon.setImageResource(R.drawable.theanimegallery);
				break;
			case Result.ZEROCHAN:
				vh.imgFavicon.setImageResource(R.drawable.zerochan);
				break;
			case Result.MANGADRAWING:
				vh.imgFavicon.setImageResource(R.drawable.mangadrawing);
				break;
			case Result.ANIMEPICTURES:
				vh.imgFavicon.setImageResource(R.drawable.animepictures);
				break;
			}
			
			imLoader.displayImage(result.thumb, vh.imgThumb, options);
		}
		
		return v;
	}
	
	private static class ViewHolder {
		TextView txtSimilarity;
		RelativeLayout wrapper;
		ImageView imgThumb;
		TextView txtTitle;
		TextView txtSize;
		ImageView imgFavicon;
	}

}
