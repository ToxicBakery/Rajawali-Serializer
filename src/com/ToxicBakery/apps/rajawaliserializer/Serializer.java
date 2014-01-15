package com.ToxicBakery.apps.rajawaliserializer;

import java.lang.Thread.UncaughtExceptionHandler;

import rajawali.Object3D;
import rajawali.parser.ParsingException;
import rajawali.util.MeshExporter;
import rajawali.util.exporter.AExporter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ToxicBakery.apps.rajawaliserializer.SerializerRenderer.ParserCallback;
import com.ToxicBakery.apps.rajawaliserializer.utils.Logger;

public class Serializer extends Activity implements Runnable,
		UncaughtExceptionHandler, OnClickListener, ParserCallback {

	// MD5 and MAX are not yet supported
	enum ParseTypes {
		THREEDS, FBX, MD2, OBJ, STL, AWD;

		public static String correctExtension(final String input) {
			if (input.indexOf("3") == 0)
				return input.replace("3", "THREE");

			return input;
		}
	}

	public static final String INTENT_LOCATION = "INTENT_LOCATION";
	public static final String INTENT_TYPE = "INTENT_TYPE";

	private static final String BUNDLE_STARTED_PARSING = "STARTED_PARSING";
	private static final String BUNDLE_FINISHED_PARSING = "FINISHED_PARSING";
	private static final int PARSING_SUCCSESSFUL = 1;
	private static final int PARSING_FAILED = 2;

	private Button mButtonStart;
	private TextView mTextViewProgress;

	private boolean startedParsing;
	private boolean finishedParsing;
	private String fileName;
	private Thread mThread;
	private ProgressDialog mProgressDialog;
	private Serializer mInstance;
	private Class<AExporter> exportType;

	private static final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			((Serializer) msg.obj)
					.setParsingStatusEnded(msg.arg1 == PARSING_SUCCSESSFUL ? R.string.serializer_parsing_finished
							: R.string.serializer_parsing_failed);
		};
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serializer);

		mInstance = this;

		if (savedInstanceState != null) {
			startedParsing = savedInstanceState.getBoolean(
					BUNDLE_STARTED_PARSING, false);
			finishedParsing = savedInstanceState.getBoolean(
					BUNDLE_FINISHED_PARSING, false);
		}

		mButtonStart = (Button) findViewById(R.id.serializerButtonStart);
		mTextViewProgress = (TextView) findViewById(R.id.serializerParsingTextview);

		mButtonStart.setOnClickListener(this);

		// Fetch the file name from the extras
		fileName = getIntent().getStringExtra(INTENT_LOCATION);
		if (fileName == null)
			// Todo need to warn the user what just happened
			finish();

		// Determine the export type
		exportType = (Class<AExporter>) getIntent().getSerializableExtra(
				INTENT_TYPE);

		Logger.i("Ready to export " + fileName + " to " + exportType);

		mThread = new Thread(this);
		mThread.setUncaughtExceptionHandler(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.serializerButtonStart:
			if (!startedParsing) {
				startedParsing = true;
				mThread.start();
				showParsingDialog();
				mButtonStart.setVisibility(View.GONE);
				mTextViewProgress.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (startedParsing) {
			if (!finishedParsing)
				showParsingDialog();

			mButtonStart.setVisibility(View.GONE);
			mTextViewProgress.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(BUNDLE_STARTED_PARSING, startedParsing);
		outState.putBoolean(BUNDLE_FINISHED_PARSING, finishedParsing);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Logger.e(ex.getMessage());
		ex.printStackTrace();
		Message msg = new Message();
		msg.obj = mInstance;
		msg.arg1 = PARSING_FAILED;
		mHandler.sendMessage(msg);
	}

	@Override
	public void run() {
		try {
			new SerializerRenderer(this, fileName, this);
		} catch (ParsingException e) {
			e.printStackTrace();
			throw new RuntimeException("Parsing failed.");
		}
	}

	@Override
	public void onParseFinished(Object3D baseObject3D) {
		final Message msg = new Message();
		msg.obj = mInstance;

		try {
			final String outFileName = fileName.substring(Environment
					.getExternalStorageDirectory().getAbsolutePath().length());
			final MeshExporter exporter = new MeshExporter(baseObject3D);
			exporter.setExportDirectory(Environment
					.getExternalStorageDirectory());
			exporter.export(outFileName, exportType, true);

			msg.arg1 = PARSING_SUCCSESSFUL;
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = PARSING_FAILED;
		}

		mHandler.sendMessage(msg);
	}

	private void setParsingStatusEnded(int msgRes) {
		finishedParsing = true;
		mTextViewProgress.setText(msgRes);
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	private void showParsingDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setTitle(R.string.serializer_parsing);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

}
