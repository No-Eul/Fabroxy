package dev.noeul.fabricmod.fabroxy;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Configs {
	private static final File CONFIG_FILE = new File("config/" + Fabroxy.ID + ".json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public boolean enabled = false;
//	public boolean onlineMode = false;
	public String secretKey = "";
	public boolean allowDirectConnection = false;

	public File getFile() {
		return CONFIG_FILE;
	}

	public void load() {
		if (!this.getFile().exists()) {
			Fabroxy.logger.info("Creating new config file...");
			this.save();
		}
		if (this.getFile().exists() && this.getFile().isFile() && this.getFile().canRead()) {
			try (FileReader reader = new FileReader(this.getFile(), StandardCharsets.UTF_8)) {
				this.setFromJsonElement(JsonParser.parseReader(reader));
			} catch (IOException e) {
				Fabroxy.logger.warn("Couldn't load the config from the file '{}'", this.getFile(), e);
			}
		} else Fabroxy.logger.info("Couldn't load the config from the file '{}'", this.getFile());

	}

	public void save() {
		File configDir = this.getFile().getAbsoluteFile().getParentFile();
		if (configDir.exists() && configDir.isDirectory() || configDir.mkdirs()) {
			try (FileWriter writer = new FileWriter(this.getFile(), StandardCharsets.UTF_8)) {
				gson.toJson(this.toJsonElement(), writer);
			} catch (IOException e) {
				Fabroxy.logger.warn("Couldn't save the config to the file '{}'", this.getFile(), e);
			}
		} else Fabroxy.logger.warn("Couldn't save the config to the file '{}'", this.getFile());
	}

	public void setFromJsonElement(JsonElement jsonElement) {
		if (jsonElement instanceof JsonObject jsonObject) {
			if (jsonObject.get("enabled") instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isBoolean()) {
				this.enabled = jsonPrimitive.getAsBoolean();
			} else Fabroxy.logger.warn("Cannot set the value '{}' of the config file '{}': Not a valid JSON boolean value", "enabled", this.getFile());

			/* if (jsonObject.get("online_mode") instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isBoolean())
				this.onlineMode = jsonPrimitive.getAsBoolean();
			else Fabroxy.logger.warn("Cannot set the value '{}' of the config file '{}': Not a valid JSON boolean value", "online_mode", this.getFile()); */

			if (jsonObject.get("secret") instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString())
				this.secretKey = jsonPrimitive.getAsString();
			else Fabroxy.logger.warn("Cannot set the value '{}' of the config file '{}': Not a valid JSON string value", "secret", this.getFile());

			if (jsonObject.get("direct_connection") instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isBoolean()) {
				this.allowDirectConnection = jsonPrimitive.getAsBoolean();
			} else Fabroxy.logger.warn("Cannot set the value '{}' of the config file '{}': Not a valid JSON boolean value", "direct_connection", this.getFile());
		} else Fabroxy.logger.warn("Cannot set values of the config file '{}': Not a valid JSON object value", this.getFile());
	}

	public JsonElement toJsonElement() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("enabled", this.enabled);
//		jsonObject.addProperty("online_mode", this.onlineMode);
		jsonObject.addProperty("secret", this.secretKey);
		jsonObject.addProperty("direct_connection", this.allowDirectConnection);
		return jsonObject;
	}
}
