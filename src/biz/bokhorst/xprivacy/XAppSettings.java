package biz.bokhorst.xprivacy;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class XAppSettings extends Activity {

	public static final String cExtraPackageName = "PackageName";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set layout
		setContentView(R.layout.xappsettings);

		// Get package name
		Bundle extras = getIntent().getExtras();
		String packageName = extras.getString(cExtraPackageName);

		// Get app info
		final ApplicationInfo appInfo;
		try {
			appInfo = getPackageManager().getApplicationInfo(packageName, 0);
		} catch (Throwable ex) {
			XUtil.bug(null, ex);
			return;
		}

		// Display app name
		TextView tvAppName = (TextView) findViewById(R.id.tvApp);
		tvAppName.setText(String.format("%s (%d)", getPackageManager().getApplicationLabel(appInfo), appInfo.uid));

		// Check if internet access
		if (XRestriction.hasInternet(this, packageName))
			findViewById(R.id.tvInternet).setVisibility(View.GONE);

		// Legend
		TextView tvUsed = (TextView) findViewById(R.id.tvUsed);
		tvUsed.setTypeface(null, Typeface.BOLD_ITALIC);
		TextView tvGranted = (TextView) findViewById(R.id.tvGranted);
		tvGranted.setTextColor(Color.GRAY);

		// Fill privacy list view adapter
		final ListView lvRestriction = (ListView) findViewById(R.id.lvRestriction);
		RestrictionAdapter privacyListAdapter = new RestrictionAdapter(this,
				android.R.layout.simple_list_item_multiple_choice, appInfo, XRestriction.getRestrictions());
		lvRestriction.setAdapter(privacyListAdapter);

		// Set privacy values
		for (int position = 0; position < lvRestriction.getAdapter().getCount(); position++) {
			String restrictionName = (String) lvRestriction.getItemAtPosition(position);
			lvRestriction.setItemChecked(position,
					XRestriction.getRestricted(null, this, appInfo.uid, restrictionName, null, false));
		}

		// Listen for privacy changes
		lvRestriction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String restrictionName = (String) lvRestriction.getItemAtPosition(position);
				boolean restricted = lvRestriction.isItemChecked(position);
				XRestriction.setRestricted(null, view.getContext(), appInfo.uid, restrictionName, null, restricted);
			}
		});
	}

	private class RestrictionAdapter extends ArrayAdapter<String> {
		private ApplicationInfo mAppInfo;

		public RestrictionAdapter(Context context, int resource, ApplicationInfo appInfo, List<String> objects) {
			super(context, resource, objects);
			mAppInfo = appInfo;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
			TextView tvRestriction = (TextView) row.findViewById(android.R.id.text1);

			// Display localize name
			String restrictionName = getItem(position);
			tvRestriction.setText(XRestriction.getLocalizedName(row.getContext(), restrictionName));

			// Display if restriction granted
			if (!XRestriction.hasPermission(row.getContext(), mAppInfo.packageName, restrictionName))
				tvRestriction.setTextColor(Color.GRAY);

			// Display if used
			if (XRestriction.isUsed(row.getContext(), mAppInfo.uid, restrictionName))
				tvRestriction.setTypeface(null, Typeface.BOLD_ITALIC);

			return row;
		}
	}
}
