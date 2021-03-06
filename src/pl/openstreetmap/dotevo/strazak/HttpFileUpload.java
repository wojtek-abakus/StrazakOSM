package pl.openstreetmap.dotevo.strazak;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class HttpFileUpload implements Runnable {

	URL connectURL;
	String responseString;
	byte[] dataToServer;
	FileInputStream fileInputStream = null;
	HttpFileUploadResponse response;
	DataOutputStream dos = null;
	File file;
	byte[] buffer;
	String name;
	int maxBufferSize = 1 * 1024 * 1024;

	HttpFileUpload(String urlString, File file, String name,
			HttpFileUploadResponse response) {
		try {
			this.name = name;
			this.file = file;
			this.response = response;
			connectURL = new URL(urlString);
		} catch (Exception ex) {
			Log.i("HttpFileUpload", "URL Malformatted");
		}
	}

	private void Sending() {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String Tag = "Upload";
		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			// Open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection) connectURL
					.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"userfile\";filename=\""
					+ name + "__" + file.getName() + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			// read file and write it into form...
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			int code = conn.getResponseCode();

			fileInputStream.close();
			dos.flush();
			dos.close();

			response.uploadResponse(code, file);
		} catch (MalformedURLException ex) {
			Log.e(Tag, "URL error: " + ex.getMessage(), ex);
		} catch (Exception e) {
			response.uploadResponse(-1, file);
			Log.e(Tag, "IO error: " + e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		Sending();
	}
}