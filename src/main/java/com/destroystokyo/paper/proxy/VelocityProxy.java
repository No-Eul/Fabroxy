package com.destroystokyo.paper.proxy;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.noeul.fabricmod.fabroxy.Fabroxy;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Identifier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class VelocityProxy {
	private static final int SUPPORTED_FORWARDING_VERSION = 1;
	public static final int MODERN_FORWARDING_WITH_KEY = 2;
	public static final int MODERN_FORWARDING_WITH_KEY_V2 = 3;
	public static final int MODERN_LAZY_SESSION = 4;
	public static final byte MAX_SUPPORTED_FORWARDING_VERSION = MODERN_LAZY_SESSION;
	public static final Identifier PLAYER_INFO_CHANNEL = new Identifier("velocity", "player_info");

	public static boolean checkIntegrity(final PacketByteBuf buf) {
		final byte[] signature = new byte[32];
		buf.readBytes(signature);

		final byte[] data = new byte[buf.readableBytes()];
		buf.getBytes(buf.readerIndex(), data);

		try {
			final Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(Fabroxy.config.secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
			final byte[] mySignature = mac.doFinal(data);
			if (!MessageDigest.isEqual(signature, mySignature)) {
				return false;
			}
		} catch (final InvalidKeyException | NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}

		return true;
	}

	public static InetAddress readAddress(final PacketByteBuf buf) {
		return InetAddresses.forString(buf.readString(Short.MAX_VALUE));
	}

	public static GameProfile createProfile(final PacketByteBuf buf) {
		final GameProfile profile = new GameProfile(buf.readUuid(), buf.readString(16));
		readProperties(buf, profile);
		return profile;
	}

	private static void readProperties(final PacketByteBuf buf, final GameProfile profile) {
		final int properties = buf.readVarInt();
		for (int i1 = 0; i1 < properties; i1++) {
			final String name = buf.readString(Short.MAX_VALUE);
			final String value = buf.readString(Short.MAX_VALUE);
			final String signature = buf.readBoolean() ? buf.readString(Short.MAX_VALUE) : null;
			profile.getProperties().put(name, new Property(name, value, signature));
		}
	}

	public static PlayerPublicKey.PublicKeyData readForwardedKey(PacketByteBuf buf) {
		return new PlayerPublicKey.PublicKeyData(buf);
	}

	public static UUID readSignerUuidOrElse(PacketByteBuf buf, UUID orElse) {
		return buf.readBoolean() ? buf.readUuid() : orElse;
	}
}
