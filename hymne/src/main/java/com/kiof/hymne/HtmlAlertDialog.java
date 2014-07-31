package com.kiof.hymne;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.webkit.WebView;

public class HtmlAlertDialog extends AlertDialog {

	protected HtmlAlertDialog(Context context, int resourceId) {
		this(context, resourceId, "", 0);
	}

	protected HtmlAlertDialog(Context context, int resourceId, String title) {
		this(context, resourceId, title, 0);
	}

	HtmlAlertDialog(Context context, int resourceId, String title,
                    int iconId) {
		super(context);

		WebView wv = new WebView(context);
		wv.setBackgroundColor(0); // transparent

		String htmlString = HtmlAlertDialog.loadRawResourceString(
				context.getResources(), resourceId);
		wv.loadData(htmlString, "text/html", "utf-8");

		if (!title.equals(""))
			this.setTitle(title);
		if (iconId != 0)
			this.setIcon(iconId);
		this.setView(wv);
		this.setButton(context.getResources().getString(R.string.Ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

	public static String loadRawResourceString(Resources res, int resourceId) {
		StringBuilder builder = new StringBuilder();
		InputStream is = res.openRawResource(resourceId);
		InputStreamReader reader = new InputStreamReader(is);
		char[] buf = new char[1024];
		int numRead = 0;
		try {
			while ((numRead = reader.read(buf)) != -1) {
				builder.append(buf, 0, numRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}

}
