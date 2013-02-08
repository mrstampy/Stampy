package asia.stampy.common.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

public class SerializationUtils {

	public static String serializeBase64(Object o) throws IOException {
		if (o instanceof byte[]) return Base64.encodeBase64URLSafeString((byte[]) o);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(o);

		return Base64.encodeBase64String(baos.toByteArray());
	}

	public static Object deserializeBase64(String s) throws IOException, ClassNotFoundException {
		byte[] bytes = Base64.decodeBase64(s);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

		return ois.readObject();
	}

}
