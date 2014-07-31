//    private void getProperties() 
//    { 
//        try 
//        { 
//           ArrayList<String> processList = new ArrayList<String>(); 
//            String line; 
//            java.lang.Process p = Runtime.getRuntime().exec("getprop"); 
//            BufferedReader input = 
//                    new BufferedReader(new 
//InputStreamReader(p.getInputStream())); 
//            while ((line = input.readLine()) != null) 
//           { 
//                System.out.println("line); //<-- 
//Parse data here. 
//                processList.add(line); 
//            } 
//            input.close(); 
//        } 
//         catch (Exception err) 
//        { 
//            err.printStackTrace(); 
//        } 
//} 
//
//
//
//SystemProperties.get("ro.MY_PROPERTY"); 
//         Log.i(TAG, "ro.MY_PROPERTY get"+value); 
//
//
//
//#include "sys/system_properties.h" 
//      char buf[32]; 
//      buf[0] = 0; 
//      __system_property_get("ro.serialno",buf); 
//Interestingly enough, i also tried this: 
//   char buf[2048]; 
//   if ((in = open("/proc/cpuinfo", O_RDONLY)) < 0) 
//      debug("can't open"); // 2mb at least, or some litebase tests 
//will just skip over. 
//   else 
//      { 
//   if (read(in, buf, sizeof(buf)) < 4) 
//      debug("can't read"); 
//   else 
//   close(in);



//	protected void postJson(final String url, final JSONObject json) {
//		Thread thread = new Thread() {
//			public void run() {
//				Looper.prepare(); // For Preparing Message Pool for the child
//				// Thread
//				DefaultHttpClient httpClient = new DefaultHttpClient();
//				HttpPost httpPost = new HttpPost(url);
//				HttpEntity httpEntity;
//				HttpResponse httpResponse = null;
//				StringEntity stringEntity = null;
//				try {
//					stringEntity = new StringEntity(httpPost.toString() + "JSON: " + json.toString());
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//				// stringEntity.setContentEncoding(new BasicHeader(HTTP.ACCEPT, "application/json"));
//				stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//				httpEntity = stringEntity;
//				httpPost.setEntity(httpEntity);
//				try {
//					httpResponse = httpClient.execute(httpPost);
//				} catch (ClientProtocolException e) {
//					e.printStackTrace();
//					Toast.makeText(mContext, "ClientProtocolException", Toast.LENGTH_SHORT).show();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				if (httpResponse != null) {
//					Toast.makeText(mContext, httpResponse.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
//					InputStream inputStream = null;
//					// Get the data in the entity
//					try {
//						inputStream = httpResponse.getEntity().getContent();
//					} catch (IllegalStateException e) {
//						e.printStackTrace();
//						Toast.makeText(mContext, "IllegalStateException", Toast.LENGTH_SHORT).show();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
////					Toast.makeText(mContext, inputStream.toString(), Toast.LENGTH_SHORT).show();
//				} else {
//					Toast.makeText(mContext, "httpResponse null", Toast.LENGTH_SHORT).show();
//				}
//				Looper.loop(); // Loop in the message queue
//			}
//		};
//		thread.start();
//	}
//
//	public static HttpResponse doPost(String url, JSONObject json) {
//		HttpClient client = new DefaultHttpClient();
//		HttpPost post = new HttpPost(url);
//		HttpEntity entity;
//		HttpResponse response = null;
//		StringEntity stringEntity;
//		try {
//			stringEntity = new StringEntity(post.toString());
//			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//			entity = stringEntity;
//			post.setEntity(entity);
//			response = client.execute(post);
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return response;
//	}
//
//	protected void sendJson(final String url, final JSONObject json) {
//		Thread t = new Thread() {
//			public void run() {
//				// For Preparing Message Pool for the child
//				Looper.prepare();
//				HttpClient client = new DefaultHttpClient();
//				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout Limit
//				HttpResponse response;
//				try {
//					HttpPost post = new HttpPost(url);
//					StringEntity stringEntity = new StringEntity("JSON: " + json.toString());
//					stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//					post.setEntity(stringEntity);
//					response = client.execute(post);
//					// Checking response
//					if (response != null) {
//						// Get the data in the entity
//						InputStream in = response.getEntity().getContent();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					Toast.makeText(mContext, "Cannot Estabilish Connection", Toast.LENGTH_SHORT).show();
//				}
//				Looper.loop(); // Loop in the message queue
//			}
//		};
//		t.start();
//	}
//
//	public static StatusLine makeRequest(String url, JSONObject json) {
//		DefaultHttpClient httpclient = new DefaultHttpClient();
//		HttpPost httpost = new HttpPost(url);
//		HttpResponse response = null;
//		StringEntity se;
//		try {
//			se = new StringEntity(json.toString());
//			httpost.setEntity(se);
//			httpost.setHeader("Accept", "application/json");
//			httpost.setHeader("Content-type", "application/json");
//			ResponseHandler responseHandler = new BasicResponseHandler();
//			response = httpclient.execute(httpost, responseHandler);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return response.getStatusLine();
//	}


//		JSONObject json = new JSONObject();

//		Log.d(TAG, "sysTime : " + sysTime);
//		Log.d(TAG, "gpsTime : " + gpsTime + "(" + gpsDelta + ")");
//		Log.d(TAG, "ntpTime : " + ntpTime + "(" + ntpDelta + ")");
//		json.put("sysTime", sysTime);
//		json.put("gpsTime", gpsTime);
//		json.put("gpsDelta", gpsDelta);
//		json.put("ntpTime", ntpTime);
//		json.put("ntpDelta", ntpDelta);

//		Log.d(TAG, "time : " + time);
//		Log.d(TAG, "duration : " + duration);
//		Log.d(TAG, "msec : " + Long.toString(msec));
//		json.put("absTime", absTime);
//		json.put("myCountry", myCountry);
//		json.put("duration", duration);
//		json.put("msec", msec);
		
		// Returns a map of names and values of all system properties.
		// This method calls System.getProperties() to get all system properties.
		// Properties whose name or value is not a String are omitted.
//		Map<Object,Object> systemProperties = System.getProperties();
//		Set<Object> keys = systemProperties.keySet();
//		for (Object key : keys) {
//			Object value = systemProperties.get(key);
//			Log.i(TAG, key + " : " + value);
//		}

		// String serialno = System.getProperty("ro.serialno");
//		json.put("os.name", System.getProperty("os.name"));
//		json.put("os.version", System.getProperty("os.version"));
//		json.put("os.arch", System.getProperty("os.arch"));
//		json.put("user.region", System.getProperty("user.region"));
//		json.put("http.agent", System.getProperty("http.agent"));

		// Build
//		json.put("board", Build.BOARD);
//		json.put("bootloader", Build.BOOTLOADER);
//		json.put("brand", Build.BRAND);
//		json.put("cpu_abi", Build.CPU_ABI);
//		json.put("cpu_abi2", Build.CPU_ABI2);
//		json.put("device", Build.DEVICE);
//		json.put("display", Build.DISPLAY);
//		json.put("fingerprint", Build.FINGERPRINT);
//		json.put("hardware", Build.HARDWARE);
//		json.put("host", Build.HOST);
//		json.put("id", Build.ID);
//		json.put("manufacturer", Build.MANUFACTURER);
//		json.put("model", Build.MODEL);
//		json.put("product", Build.PRODUCT);
//		json.put("radio", Build.RADIO);
//		json.put("serial", Build.SERIAL);
//		json.put("tags", Build.TAGS);
//		json.put("time", Build.TIME);
//		json.put("type", Build.TYPE);
//		json.put("unknown", Build.UNKNOWN);
//		json.put("user", Build.USER);
//		
//		Log.i(TAG, json.toString());
			
//		HttpResponse response = doPost("http://kiof.free.fr/stats.php?", json);
//		Log.i(TAG, "doPost response : " + response);
//		sendJson("http://kiof.free.fr/stats.php?", json);
//		Log.i(TAG, "sendJson");
//		StatusLine status = makeRequest("http://kiof.free.fr/stats.php?", json);
//		Log.i(TAG, "makeRequest status : " + status);

//		postJson("http://kiof.free.fr/stats.php?", json);
//		Log.i(TAG, "postJson");
