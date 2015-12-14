package org.iqdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class IqdbSearchActivity extends ListActivity implements OnItemClickListener {
	private TextView txtEmpty;
	public ArrayList<Result> mResults;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();
		
		
		
		setContentView(R.layout.results);
		
		txtEmpty = (TextView)findViewById(android.R.id.empty);
		ResultsAdapter adapter = new ResultsAdapter(this);
		
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(this);
		
		
		if(savedInstanceState != null && savedInstanceState.containsKey("results")){
			mResults = savedInstanceState.getParcelableArrayList("results");
			for (Result result : mResults) {
				adapter.add(result);
			}
			return;
		}
		
		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				try {
					long statSize = getContentResolver().openFileDescriptor(
							uri, "r").getStatSize();
					if (statSize < 8388608) {
						String filePath = uri.toString(); //parseUriToFilename(uri);
						(new UploadImageTask(this)).execute(filePath, "file");
					} else {
						txtEmpty.setText(R.string.image_must_be_less_8192_kb);
						//browser.loadData("Изображение должно быть меньше 8192кб", "text/html", "UTF-8");
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					txtEmpty.setText(R.string.file_not_found);
					//browser.loadData("Файл не найден", "text/html", "UTF-8");
				}

				// Log.e("TEST", uri.toString());
				// Log.e("FILENAME", filePath);
			}
			if (extras.containsKey(Intent.EXTRA_TEXT)) {
				String uri = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
				try {
					new URL(uri);
					if (uri != null && (uri.endsWith("png") || uri.endsWith("gif")
							|| uri.endsWith("jpg") || uri.endsWith("jpeg"))) {
						(new UploadImageTask(this)).execute(uri, "url");
					} else {
						finish();
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			//	Log.e("TESTURL", uri);
			}

		}
		//Log.e("TEST", intent.getType());
	}

	class UploadImageTask extends AsyncTask<String, Integer, ArrayList<Result>> implements OnDismissListener {

		private ProgressDialog dialog;
		//private TextView dtext;
		private Context context;

		public UploadImageTask(Context context) {
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			dialog = new ProgressDialog(context);
			//dialog.setView(dtext);
			//dialog.setTitle("Загрузка на iqdb.org");
			dialog.show();
			dialog.setOnDismissListener(this);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<Result> doInBackground(String... params) {
			String result = "";
			String filename_or_uri = params[0];
			String type = params[1];
			
			if ("url".equals(type)) {
				try {
					/*
					runOnUiThread(new Runnable() {
						
						public void run() {*/
							dialog.setMessage(context.getString(R.string.search_by_url));	
						/*}
					});*/
					InputStream is = (InputStream) (new URL(
							"http://iqdb.org/?url=" + filename_or_uri))
							.getContent();
					StringBuilder sb = new StringBuilder();
					InputStreamReader reader = new InputStreamReader(is);

					int c;

					while ((c = reader.read()) != -1) {
						sb.append((char) c);
					}
					result = sb.toString();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if ("file".equals(type)) {
				/*runOnUiThread(new Runnable() {
					
					public void run() {*/
						dialog.setMessage(context.getString(R.string.search_by_file));		
					/*}
				});*/
				
				try {
					result = upload2iqdb(filename_or_uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			/*
			try {
				InputStream testfile = getAssets().open("iqdb-result.html");
				
				int c;
				StringBuilder resultbuilder = new StringBuilder();
				while((c = testfile.read()) != -1) {
					resultbuilder.append((char)c);
				}
				result = resultbuilder.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
            return parseHtml(result);
		}

		@Override
		protected void onPostExecute(ArrayList<Result> results) {
			if (getStatus() == Status.RUNNING && dialog.isShowing()) {
				dialog.dismiss();
			}

			
			ResultsAdapter adapter = (ResultsAdapter)getListAdapter();
			adapter.clear();
			
			mResults = results;
			for (Result result : results) {
				adapter.add(result);	
			}

			super.onPostExecute(results);
		}

		public void onDismiss(DialogInterface dialog) {
			this.cancel(true);
		}

	}

	/*
	private String parseUriToFilename(Uri uri) {
		// Получим имя изображения из URI
		//не работает на реальной железке
		String[] filepathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, filepathColumn, null, null, null);
		int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		Log.e("COLUMN INDEX", columnIndex + "");
		cursor.moveToFirst();
		String filePath = cursor.getString(columnIndex);
		// cursor.close();
		return filePath;
	}*/

	public ArrayList<Result> parseHtml(String html) {
		ArrayList<Result> results = new ArrayList<Result>();
		Pattern pattern = Pattern
				.compile("<div>(<table><tr><th>.+?</table>)</div>");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			String single = matcher.group(1);
			Result r = Result.parseHtml(single);
			results.add(r);
		}
		return results;
	}

	private String upload2iqdb(String filename) throws IOException {
		URL url = new URL("http://iqdb.org/");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection
				.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20120405 Firefox/14.0a1");
		connection.setRequestProperty("Content-Language", "ru-RU");
		connection.setRequestProperty("Connection", "Keep-Alive");
		String BOUNDRY = "WAEFQ@@#2qf23qDFgerG$EJ6[4pw5hk0w93wkfowwmeg";
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDRY);

		StringBuilder requestBody = new StringBuilder();
		requestBody.append("--")
				.append(BOUNDRY)
				.append('\n')
				.append("Content-Disposition: form-data; name='description'\n\n")
				.append(filename).append("\n")
				.append("--")
				.append(BOUNDRY)
				.append('\n').append("Content-Disposition: form-data; name='file'; filename='")
				.append(filename)
				.append("'\n")
				.append("Content-Type: application/octet-stream\n")
				.append("Content-Transfer-Encoding: binary\n")
				.append('\n');

		int bytesAvailable, bufferSize, bytesRead, endBlockSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		
		Uri imageUri = Uri.parse(filename);
		//Get bitmap
		Bitmap bitmap = Media.getBitmap(getContentResolver(), imageUri);
		
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		float ratio = width * 1.f / height;
		
		int newHeight = 0, newWidth = 0;
		
		if(width > height){
			//landscape
			if(width > 640){
				newWidth = 640;
				newHeight = (int)(newWidth / ratio); 
			} else if(height > 480) {
				newHeight = 480;
				newWidth = (int)(newHeight * ratio);
			}
		} else {
			//portrait
			if(height > 640){
				newHeight = 640;
				newWidth =  (int)(newHeight * ratio);
			} else if(width > 480) {
				newWidth = 480;
				newHeight = (int)(newWidth * ratio);
			}
		}
		
		if(newWidth != 0 && newHeight != 0){
			bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
		}
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
		InputStream fileInputStream = new ByteArrayInputStream(outStream.toByteArray());
		//end get bitmap
		
		
		//InputStream fileInputStream = getContentResolver().openInputStream(Uri.parse(filename));
		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];

		endBlockSize = BOUNDRY.length() + 6;

		connection.setRequestProperty(
				"Content-Length",
				String.valueOf(requestBody.toString().length() + bufferSize
						+ endBlockSize));

		connection.connect();

		DataOutputStream dataOS = new DataOutputStream(
				connection.getOutputStream());
		dataOS.writeBytes(requestBody.toString());

		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		dataOS.write(buffer, 0, bytesRead);

		dataOS.writeBytes("\n");
		dataOS.writeBytes("--");
		dataOS.writeBytes(BOUNDRY);
		dataOS.writeBytes("--");
		dataOS.writeBytes("\n");

		fileInputStream.close();

		// dataOS.flush();
		dataOS.close();

		int responseCode = connection.getResponseCode();
		String resultString;
		if (responseCode == 200) {
			// если все прошло нормально, получаем результат
			// может быть другой код, см.
			// http://developer.android.com/reference/java/net/HttpURLConnection.html

			InputStream in = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(in, "UTF-8");

			StringBuilder data = new StringBuilder();
			int c;
			while ((c = isr.read()) != -1) {
				data.append((char) c);
			}

			resultString = data.toString();

		} else {
			resultString = "сервер не ответил";
		}

		bitmap.recycle();
		
		return resultString;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		ResultsAdapter adapter = (ResultsAdapter) getListAdapter();
		Result result = adapter.getItem(position);
		if (result.url != null) {
            if(result.url.startsWith("//")){
                result.url = "http:" + result.url;
            }

			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(result.url));
			startActivity(browserIntent);
			//finish();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if(mResults != null){
			outState.putParcelableArrayList("results", mResults);
		}
	}
}