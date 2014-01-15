package com.ToxicBakery.apps.rajawaliserializer;

import java.io.File;
import java.util.Locale;

import rajawali.Object3D;
import rajawali.parser.AMeshLoader;
import rajawali.parser.Loader3DSMax;
import rajawali.parser.LoaderAWD;
import rajawali.parser.LoaderMD2;
import rajawali.parser.LoaderOBJ;
import rajawali.parser.LoaderSTL;
import rajawali.parser.ParsingException;
import rajawali.parser.fbx.LoaderFBX;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

import com.ToxicBakery.apps.rajawaliserializer.Serializer.ParseTypes;
import com.ToxicBakery.apps.rajawaliserializer.utils.Logger;

public class SerializerRenderer extends RajawaliRenderer {

	public SerializerRenderer(Context context, String filename,
			ParserCallback parseCallback) throws ParsingException {
		super(context);

		final File file = new File(filename);
		final ParseTypes parseType;

		// validate the file type
		try {
			parseType = ParseTypes.valueOf(ParseTypes.correctExtension(filename
					.substring(filename.lastIndexOf(".") + 1).toUpperCase(
							Locale.getDefault())));
		} catch (Exception e) {
			throw new RuntimeException("Unknown model type.");
		}

		final Object3D obj;

		AMeshLoader parser = null;
		switch (parseType) {
		case THREEDS:
			parser = new Loader3DSMax(this, file);
			break;
		case FBX:
			parser = new LoaderFBX(this, file);
			break;
		case MD2:
			parser = new LoaderMD2(this, file);
			break;
		case OBJ:
			parser = new LoaderOBJ(this, file);
			break;
		case STL:
			parser = new LoaderSTL(this, file);
			break;
		case AWD:
			parser = new LoaderAWD(this, file);
		default:
			break;
		}

		obj = parser.parse().getParsedObject();

		parseCallback.onParseFinished(obj);
		Logger.i("Parsing successful.");
	}

	public interface ParserCallback {
		void onParseFinished(Object3D baseObject3D);
	}

}
