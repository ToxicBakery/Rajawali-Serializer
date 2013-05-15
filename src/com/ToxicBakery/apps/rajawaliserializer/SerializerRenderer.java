package com.ToxicBakery.apps.rajawaliserializer;

import java.io.File;
import java.util.Locale;

import rajawali.BaseObject3D;
import rajawali.parser.AMeshParser;
import rajawali.parser.MD2Parser;
import rajawali.parser.ObjParser;
import rajawali.parser.StlParser;
import rajawali.parser.fbx.FBXParser;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

import com.ToxicBakery.apps.rajawaliserializer.Serializer.ParseTypes;
import com.ToxicBakery.apps.rajawaliserializer.utils.Logger;

public class SerializerRenderer extends RajawaliRenderer {
	
	public SerializerRenderer(Context context, String filename,
			ParserCallback parseCallback) {
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
			Logger.i("Parsing successful.");
		} catch (Exception e) {
			Logger.e("Parsing failed.");
			e.printStackTrace();
			throw new RuntimeException("Failed to parse model.", e);
		}
	}

	public interface ParserCallback {
		void onParseFinished(BaseObject3D baseObject3D);
	}
	
}
