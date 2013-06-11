package com.ToxicBakery.apps.rajawaliserializer;

import java.io.File;
import java.util.Locale;

import rajawali.BaseObject3D;
import rajawali.parser.AMeshParser;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.AWDParser;
import rajawali.parser.MD2Parser;
import rajawali.parser.Max3DSParser;
import rajawali.parser.ObjParser;
import rajawali.parser.StlParser;
import rajawali.parser.fbx.FBXParser;
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
			// TODO Clean up this mess, duplicate of what is in ModelPicker..
			parseType = ParseTypes.valueOf(ParseTypes.correctExtension(filename
					.substring(filename.lastIndexOf(".") + 1).toUpperCase(
							Locale.getDefault())));
		} catch (Exception e) {
			throw new RuntimeException("Unknown model type.");
		}

		final BaseObject3D obj;

		// AWD parsing does not extend AMeshParser for various reason and needs special handling
		if (parseType == ParseTypes.AWD) {
			final AWDParser parser = new AWDParser(this, file);
			parser.parse();
			obj = parser.getParsedObject(false);

		} else {

			// The majority of parsers can be used normally
			AMeshParser parser = null;
			switch (parseType) {
			case THREEDS:
				parser = new Max3DSParser(this, file);
				break;
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
			case AWD: // Handled previously
			default:
				break;
			}

			obj = parser.parse().getParsedObject();
		}

		parseCallback.onParseFinished(obj);
		Logger.i("Parsing successful.");
	}

	public interface ParserCallback {
		void onParseFinished(BaseObject3D baseObject3D);
	}

}
