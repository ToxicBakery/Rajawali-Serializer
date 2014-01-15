package com.ToxicBakery.apps.rajawaliserializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rajawali.util.exporter.AwdExporter;
import rajawali.util.exporter.SerializationExporter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ToxicBakery.apps.rajawaliserializer.Serializer.ParseTypes;
import com.ToxicBakery.apps.rajawaliserializer.adapters.FileBrowserAdapter;
import com.ToxicBakery.apps.rajawaliserializer.utils.Logger;

/**
 * Provides a basic file browser with simple forward/backward controls. This is based on the File
 * class and its helper methods. The class name File is misleading as a directory is also accessible
 * through the File class.
 * 
 * @author TencenT
 * 
 */
public class ModelPicker extends ListActivity {

	enum DialogType {
		EXIT, SERIALIZE, WARNING;
	}

	public static final String FILE_NAME = new String("SELECTED_FILE");

	private static final String BUNDLE_DIALOG = "dialog";
	private static final String BUNDLE_LOCATION = "location";
	private static final String BUNDLE_SERIALIZE_INTENT = "serializeIntent";

	private static final String STATE_DIALOG_CLOSED = "closed";
	private static final String STATE_DIALOG_OPEN = "open";

	private static final File SDCARD = Environment
			.getExternalStorageDirectory();

	private Context context;
	private Dialog dialogMessage;
	private File location = SDCARD;
	private List<File> fileList = new ArrayList<File>();
	private String[] dialogMessageData;

	private static Intent intentSerializer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		intentSerializer = new Intent(this, Serializer.class);
		if (savedInstanceState != null) {
			Logger.i("Loading saved state");
			try {
				// Convert the stored path of the directory to a File object
				Logger.i("Loading saved location");
				location = new File(
						savedInstanceState.getString(BUNDLE_LOCATION));

				// load the dialog data
				if (savedInstanceState.getStringArray(BUNDLE_DIALOG) != null) {
					dialogMessageData = savedInstanceState
							.getStringArray(BUNDLE_DIALOG);
				}

				intentSerializer = savedInstanceState
						.getParcelable(BUNDLE_SERIALIZE_INTENT);
			} catch (Exception e) {
			}
		}

		// Update the application title
		setTitle(location.getAbsolutePath());
		if (!location.isDirectory() && location.getParent() != null) {
			setTitle(location.getParent());
			updateList();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_model_picker, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!location.getAbsolutePath().equals(SDCARD.getAbsolutePath())) {
				navigateUp();
				updateList();
			} else {
				displayMessage(R.string.close_dialog_title,
						R.string.close_dialog_message, DialogType.EXIT);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(context, Settings.class));
			break;
		case R.id.menu_exit:
			finish();
			break;
		// case R.id.menu_root:
		case android.R.id.home:
			location = new File("/");
			updateList();
			break;
		case R.id.menu_sdcard:
			location = Environment.getExternalStorageDirectory();
			updateList();
			break;
		case R.id.menu_up:
			navigateUp();
			updateList();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (dialogMessage != null && dialogMessage.isShowing()) {
			dialogMessage.dismiss();
			dialogMessageData[2] = STATE_DIALOG_OPEN;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();

		// If a dialog was previously displayed, reopen it.
		if (dialogMessageData != null
				&& dialogMessageData[2] == STATE_DIALOG_OPEN) {
			if (DialogType.valueOf(dialogMessageData[3]) != DialogType.SERIALIZE) {
				displayMessage(dialogMessageData[0], dialogMessageData[1],
						DialogType.valueOf(dialogMessageData[3]));
			} else {
				displaySerializeMessage();
			}
			dialogMessageData[2] = STATE_DIALOG_CLOSED;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.i("Saving state");

		// Bundle can not save classes so we have to save the path to the
		// current directory
		Logger.i("Saving location");
		outState.putString(BUNDLE_LOCATION, location.getAbsolutePath());

		// If a dialog is being displayed, save the dialog for viewing
		if (dialogMessageData != null
				&& dialogMessageData[2] == STATE_DIALOG_OPEN) {
			Logger.i("Saving dialog state");
			outState.putStringArray(BUNDLE_DIALOG, dialogMessageData);
		}

		outState.putParcelable(BUNDLE_SERIALIZE_INTENT, intentSerializer);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		File selectedFile = new File(fileList.get(position).getAbsolutePath());
		Logger.i("Selected: " + selectedFile.getAbsolutePath());
		intentSerializer.putExtra(Serializer.INTENT_LOCATION,
				selectedFile.getAbsolutePath());
		if (selectedFile.isDirectory()) {
			if (selectedFile.canRead()) {
				String fileName = fileList.get(position).getName();
				if (fileName != "." && fileName != "..") {
					location = selectedFile;
				} else if (fileName == "..") {
					navigateUp();
				}
				updateList();
			} else {
				displayMessage(R.string.error_read_access_title,
						R.string.error_read_access_message, DialogType.WARNING);
			}
		} else {

			// Try opening the file for parsing
			try {
				// Determine the extension of the file
				String extension = selectedFile.getName();
				try {
					// TODO Clean up this mess, duplicate of what is in SerializerRenderer..
					extension = extension.substring(
							extension.lastIndexOf(".") + 1).toUpperCase(
							Locale.getDefault());
					ParseTypes.valueOf(ParseTypes.correctExtension(extension));
				} catch (Exception e) {
					e.printStackTrace();
					displayMessage(R.string.error_not_parsable_title,
							R.string.error_not_parsable_message,
							DialogType.WARNING);
					return;
				}
				if (selectedFile.canRead() && selectedFile.canWrite()) {
					displaySerializeMessage();
				}
			} catch (Exception e) {
				e.printStackTrace();
				displayMessage(R.string.error_not_parsable_title,
						R.string.error_not_parsable_message, DialogType.WARNING);
			}
		}
	}

	private void displaySerializeMessage() {
		final String title = getResources().getString(
				R.string.serialize_dialog_title);

		dialogMessageData = new String[] { title, "", "",
				DialogType.SERIALIZE.toString() };

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.output_choices));

		final OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:// AWD
					intentSerializer.putExtra(Serializer.INTENT_TYPE,
							AwdExporter.class);
					break;
				case 1:// Serialize
					intentSerializer.putExtra(Serializer.INTENT_TYPE,
							SerializationExporter.class);
					break;
				}
				startActivity(intentSerializer);
			}
		};

		final Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setAdapter(adapter, clickListener);

		dialogMessage = builder.create();
		dialogMessage.show();
	}

	/**
	 * Helper for constructing common dialogs.
	 * 
	 * @param resTitle
	 * @param resMessage
	 * @param type
	 */
	private void displayMessage(int resTitle, int resMessage, DialogType type) {
		displayMessage(getString(resTitle), getString(resMessage), type);
	}

	/**
	 * Helper for constructing common dialogs.
	 * 
	 * @param title
	 * @param message
	 * @param type
	 */
	private void displayMessage(String title, String message,
			final DialogType type) {
		dialogMessageData = new String[] { title, message, "", type.toString() };
		Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(android.R.string.ok,
				new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialogMessageData = null;
						dialogMessage.cancel();
						switch (type) {
						case EXIT:
							finish();
							break;
						case WARNING:
						default:
							break;
						}
					}
				});
		switch (type) {
		case EXIT:
			builder.setNegativeButton(android.R.string.cancel,
					new Dialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialogMessageData = null;
							dialogMessage.cancel();
						}
					});
			break;
		default:
			break;
		}
		dialogMessage = builder.create();
		dialogMessage.show();
	}

	/**
	 * Navigate up in the directory if capable and as long as the user is still inside the sdcard
	 * directory.
	 */
	private void navigateUp() {
		if (location.getParentFile() != null
				&& location.getParentFile().getAbsolutePath()
						.contains(SDCARD.getAbsolutePath())) {
			location = location.getParentFile();
		}
	}

	/**
	 * Update the list view with list of files for the current directory.
	 */
	private void updateList() {

		setTitle(location.getAbsolutePath());

		// Empty the current list of files
		fileList.clear();

		// Add . and .. for refresh and navigation.
		fileList.add(new File("."));
		if (!location.getAbsolutePath().equals(SDCARD.getAbsolutePath())
				&& location.getParent() != null
				&& location.getAbsolutePath() != "/") {
			fileList.add(new File(".."));
		}

		// Fetch a new list of files
		File[] fileArray = location.listFiles();
		for (File file : fileArray) {
			fileList.add(file);
		}

		// Update the list view with the new data
		setListAdapter(new FileBrowserAdapter(context,
				R.layout.row_file_browser, R.id.rowFileBrowser_name, fileList));
	}
}