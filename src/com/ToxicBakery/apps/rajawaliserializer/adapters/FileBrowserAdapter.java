package com.ToxicBakery.apps.rajawaliserializer.adapters;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ToxicBakery.apps.rajawaliserializer.R;

public class FileBrowserAdapter extends ArrayAdapter<File> {

	private Context context;
	private List<File> files;

	public FileBrowserAdapter(Context context, int resource,
			int textViewResourceId, List<File> files) {
		super(context, resource, textViewResourceId, files);
		this.context = context;
		this.files = files;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			// Create the inflater and inflate the xml data for the row
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.row_file_browser, parent,
					false);

			// Find the image and text views in the row
			holder.icon = (ImageView) convertView
					.findViewById(R.id.rowFileBrowser_icon);
			holder.fileName = (TextView) convertView
					.findViewById(R.id.rowFileBrowser_name);
			holder.filePermissions = (TextView) convertView
					.findViewById(R.id.rowFileBrowser_permissions);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Get the file
		final File file = files.get(position);

		// Set the image
		// TODO Add image to identify parsable types.
		if (file.isDirectory()) {
			holder.icon.setBackgroundResource(R.drawable.blue_folder);
		} else {
			holder.icon.setBackgroundResource(R.drawable.blue_document);
		}

		// Determine the permissions
		String permissions = "";
		if (file.canWrite()) {
			permissions = "RW";
		} else if (file.canRead()) {
			permissions = "RO";
		}

		// Set the file name and permissions
		holder.fileName.setText(file.getName());
		holder.filePermissions.setText(permissions);

		return convertView;
	}

	private static class ViewHolder {
		public ImageView icon;
		public TextView fileName;
		public TextView filePermissions;
	}

}
