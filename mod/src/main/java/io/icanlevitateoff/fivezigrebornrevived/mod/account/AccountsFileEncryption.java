/*
 * Copyright (c) 2019-2020 5zig Reborn
 * Copyright (c) 2026 IcanLevitate
 * Copyright (c) 2015-2019 5zig
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.icanlevitateoff.fivezigrebornrevived.mod.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Reads and writes the password-protected accounts.enc.
 *
 * <p>Current format: {@code "5ZRR"}, format version 2 (1 byte), PBKDF2 iterations (4 bytes), salt
 * (16 bytes), nonce (12 bytes), then the accounts JSON encrypted with AES-256-GCM, whose tag also
 * detects a wrong password or a modified file. Salt and nonce are new on every save.
 *
 * <p>Files written by older versions (salt 64, IV 16, AES-128-CBC, 10,000 iterations) are still
 * read; {@link #needsUpgrade()} then tells the caller to save again in the current format.
 *
 * Originally created by RoccoDev on 2019-08-25.
 */
public class AccountsFileEncryption {

	private static final byte[] MAGIC = {'5', 'Z', 'R', 'R'};
	private static final int FORMAT = 2;
	private static final int ITERATIONS = 210_000;
	private static final int SALT_LENGTH = 16;
	private static final int NONCE_LENGTH = 12;
	private static final int TAG_BITS = 128;
	private static final int MAX_ITERATIONS = 10_000_000;

	private static final SecureRandom RANDOM = new SecureRandom();

	private static volatile boolean needsUpgrade;

	private static File file() {
		return new File(The5zigMod.getModDirectory(), "accounts.enc");
	}

	/** True after {@link #decrypt} read a file in the old format. */
	public static boolean needsUpgrade() {
		return needsUpgrade;
	}

	public static synchronized void encrypt(String password, JsonObject jsonIn) throws Exception {
		byte[] salt = new byte[SALT_LENGTH];
		byte[] nonce = new byte[NONCE_LENGTH];
		RANDOM.nextBytes(salt);
		RANDOM.nextBytes(nonce);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt, ITERATIONS, 256), new GCMParameterSpec(TAG_BITS, nonce));
		byte[] header = header(salt, nonce);
		cipher.updateAAD(header);
		byte[] encrypted = cipher.doFinal(jsonIn.toString().getBytes(StandardCharsets.UTF_8));

		ByteArrayOutputStream out = new ByteArrayOutputStream(header.length + encrypted.length);
		out.write(header);
		out.write(encrypted);

		// Write next to the file and move it over, so a crash mid-write cannot destroy the accounts.
		File target = file();
		File temp = new File(target.getParentFile(), target.getName() + ".tmp");
		Files.write(temp.toPath(), out.toByteArray());
		try {
			Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		needsUpgrade = false;
	}

	public static synchronized JsonObject decrypt(String password) throws Exception {
		byte[] data = Files.readAllBytes(file().toPath());
		byte[] plain;
		if (data.length > MAGIC.length && Arrays.equals(Arrays.copyOf(data, MAGIC.length), MAGIC)) {
			plain = decryptCurrent(password, data);
			needsUpgrade = false;
		} else {
			plain = decryptLegacy(password, data);
			needsUpgrade = true;
		}
		return JsonParser.parseString(new String(plain, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	private static byte[] decryptCurrent(String password, byte[] data) throws Exception {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.position(MAGIC.length);
		int format = buffer.get() & 0xFF;
		if (format != FORMAT) {
			throw new IOException("accounts.enc was written by a newer version of the mod (format " + format + ")");
		}
		int iterations = buffer.getInt();
		if (iterations <= 0 || iterations > MAX_ITERATIONS) {
			throw new IOException("accounts.enc is damaged");
		}
		byte[] salt = new byte[SALT_LENGTH];
		byte[] nonce = new byte[NONCE_LENGTH];
		buffer.get(salt);
		buffer.get(nonce);
		int headerLength = buffer.position();
		byte[] encrypted = new byte[buffer.remaining()];
		buffer.get(encrypted);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt, iterations, 256), new GCMParameterSpec(TAG_BITS, nonce));
		cipher.updateAAD(data, 0, headerLength);
		return cipher.doFinal(encrypted);
	}

	private static byte[] decryptLegacy(String password, byte[] data) throws Exception {
		if (data.length < 64 + 16 + 16) {
			throw new IOException("accounts.enc is damaged");
		}
		byte[] salt = Arrays.copyOfRange(data, 0, 64);
		byte[] iv = Arrays.copyOfRange(data, 64, 80);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt, 10_000, 128), new IvParameterSpec(iv));
		return cipher.doFinal(data, 80, data.length - 80);
	}

	private static byte[] header(byte[] salt, byte[] nonce) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.write(MAGIC);
		out.writeByte(FORMAT);
		out.writeInt(ITERATIONS);
		out.write(salt);
		out.write(nonce);
		return bytes.toByteArray();
	}

	private static SecretKey deriveKey(String password, byte[] salt, int iterations, int bits) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, bits);
		try {
			byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(spec).getEncoded();
			return new SecretKeySpec(key, "AES");
		} finally {
			spec.clearPassword();
		}
	}
}
