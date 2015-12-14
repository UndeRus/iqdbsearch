package org.iqdb;

import android.app.Application;
import android.content.Context;
import android.os.Process;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class IqdbSearch extends Application {

		@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			super.onCreate();
			initImageLoader(getApplicationContext());
		}

		private void initImageLoader(Context context) {
			// TODO Auto-generated method stub
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.threadPriority(Process.THREAD_PRIORITY_BACKGROUND)
			.denyCacheImageMultipleSizesInMemory()
            .diskCacheFileCount(10)
			.tasksProcessingOrder(QueueProcessingType.LIFO)
            //.writeDebugLogs()
			.build();
			
			ImageLoader.getInstance().init(config);
		}
		
		
}
