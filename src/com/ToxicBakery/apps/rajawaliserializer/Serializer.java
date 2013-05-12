package com.ToxicBakery.apps.rajawaliserializer;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

import rajawali.BaseObject3D;
import rajawali.parser.AMeshParser;
import rajawali.parser.MD2Parser;
import rajawali.parser.ObjParser;
import rajawali.parser.StlParser;
import rajawali.parser.fbx.FBXParser;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.MeshExporter;
import rajawali.util.MeshExporter.ExportType;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ToxicBakery.apps.rajawaliserializer.Serializer.SerializerRender.ParseCallback;

public class Serializer extends Activity implements Runnable,
		UncaughtExceptionHandler, OnClickListener {

	// MD5 and MAX are not yet supported
	enum ParseTypes {
		FBX, MD2, OBJ, STL;
	}

	public static final String INTENT_LOCATION = "INTENT_LOCATION";

	private static final String BUNDLE_STARTED_PARSING = "STARTED_PARSING";
	private static final String BUNDLE_FINISHED_PARSING = "FINISHED_PARSING";
	private static final int PARSING_SUCCSESSFUL = 1;
	private static final int PARSING_FAILED = 2;

	private Button mButtonStart;
	private TextView mTextViewProgress;

	private boolean startedParsing;
	private boolean finishedParsing;
	private String fileName;
	private ParseCallback callback;
	private Thread mThread;
	private ProgressDialog mProgressDialog;
	private Serializer mInstance;

	private static final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			((Serializer) msg.obj)
					.setParsingStatusEnded(msg.arg1 == PARSING_SUCCSESSFUL ? R.string.serializer_parsing_finished
							: R.string.serializer_parsing_failed);
		};
	};

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
			finish();

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
		Message msg = new Message();
		msg.obj = mInstance;
		msg.arg1 = PARSING_FAILED;
		mHandler.sendMessage(msg);
	}

	@Override
	public void run() {
		callback = new ParseCallback() {
			@Override
			public void onParseFinished(BaseObject3D baseObject3D) {
				try {
					final String outFileName = (fileName + ".ser")
							.substring(Environment
									.getExternalStorageDirectory()
									.getAbsolutePath().length());
					final MeshExporter exporter = new MeshExporter(baseObject3D);
					exporter.setExportDirectory(Environment.getExternalStorageDirectory());
					exporter.export(outFileName, ExportType.SERIALIZED);

					Message msg = new Message();
					msg.obj = mInstance;
					msg.arg1 = PARSING_SUCCSESSFUL;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.obj = mInstance;
					msg.arg1 = PARSING_FAILED;
					mHandler.sendMessage(msg);
				}
			}
		};

		new SerializerRender(this, fileName, callback);
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

	protected static final class SerializerRender extends RajawaliRenderer {

		public SerializerRender(Context context, String filename,
				ParseCallback parseCallback) {
			super(context);

			final File file = new File(filename);
			final ParseTypes parseType;

			AMeshParser parser = null;

			// validate the file type
			try {
				parseType = ParseTypes.valueOf(filename.substring(
						filename.lastIndexOf(".") + 1).toUpperCase(
						Locale.getDefault()));
			} catch (Exception e) {
				throw new RuntimeException("Unknown model type.");
			}

			// create the proper parser
			switch (parseType) {
			case FBX:
				parser = new FBXParser(this, file);
				break;
			case MD2:
				parser = new MD2Parser(this, file);
				break;
			case OBJ:
				parser = new ObjParser(this, file);
				break;
			case STL:
				parser = new StlParser(this, file);
				break;
			}

			// parse and hand back to the callback
			try {
				BaseObject3D obj = parser.parse().getParsedObject();
				parseCallback.onParseFinished(obj);
				System.out.println("Successfully completed.");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Failed to parse model.");
			}
		}

		public interface ParseCallback {
			void onParseFinished(BaseObject3D baseObject3D);
		}

	}

}
